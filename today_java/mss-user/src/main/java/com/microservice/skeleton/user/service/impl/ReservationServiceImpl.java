// ReservationServiceImpl.java
package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.*;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.Request.ReservationRequest;
import com.microservice.skeleton.user.domain.Response.ReservationResponse;
import com.microservice.skeleton.user.domain.Response.RoomReservationStatusResponse;
import com.microservice.skeleton.user.domain.Response.RoomResponse;
import com.microservice.skeleton.user.domain.dto.TimeRangeDto;
import com.microservice.skeleton.user.domain.entity.*;
import com.microservice.skeleton.user.domain.vo.ReservationVO;
import com.microservice.skeleton.user.domain.vo.TimePointVO;
import com.microservice.skeleton.user.mapper.ReservationMapper;
import com.microservice.skeleton.user.mapper.RoomReserveDateMapper;
import com.microservice.skeleton.user.service.*;
import com.microservice.skeleton.user.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation>
        implements ReservationService {

    @Autowired
    private TimePointService timePointService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReservationApprovalService reservationApprovalService;

    @Autowired
    private UserPenaltyService penaltyService;

    @Autowired
    private RedisDelayQueueService redisDelayQueueService;

    @Autowired
    private RoomReserveDateMapper roomReserveDateMapper;

    @Autowired
    private RoomReserveDateService roomReserveDateService;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        String openid = UserContext.getCurrentOpenid();
        if (openid == null || openid.trim().isEmpty()) {
             log.error("创建预约失败：用户ID为空，无法继续操作");
             throw new BusinessException("用户未登录或身份验证失败，请重新登录后再试");
        }

        List<Integer> timePointIds = request.getTimePointIds();
        if (timePointIds == null || timePointIds.size() < 2) {
            throw new BusinessException("至少选择2个时间点（开始和结束）");
        }

        // 2. 获取所有时间点数据，并按时间排序（确保 startTime < endTime）
        List<TimePoint> timePoints = timePointService.listByIds(timePointIds);
        if (timePoints.size() != timePointIds.size()) {
            throw new BusinessException("部分时间点数据不存在");
        }
        timePoints.sort(Comparator.comparing(TimePoint::getPoint));
        TimePoint startPoint = timePoints.get(0);
        TimePoint endPoint = timePoints.get(timePoints.size() - 1);
        if (startPoint.getPoint().equals(endPoint.getPoint())) {
            throw new BusinessException("开始时间和结束时间不能相同");
        }

        LocalDate reservationDate = request.getReservationDate();
        Long roomId = request.getRoomId();
        Integer startTimeId = startPoint.getId();
        Integer endTimeId = endPoint.getId();

        // ========== 关键新增：先初始化该教室+日期的基础时间点记录 ==========
        // 确保room_reserve_date表中有该教室+该日期的所有时间点基础记录（status=1）
        initRoomReserveDate(roomId.intValue(), reservationDate);

        // ========== 新增修改1：解析区间内所有时间点ID ==========
        // 查询系统中所有时间点，按时间排序
        List<TimePoint> allSystemTimePoints = timePointService.list();
        allSystemTimePoints.sort(Comparator.comparing(TimePoint::getPoint));
        // 筛选出「开始ID ≤ 时间点ID ≤ 结束ID」的所有时间点（区间内所有）
        List<Integer> rangeTimePointIds = allSystemTimePoints.stream()
                .filter(tp -> tp.getId() >= startTimeId && tp.getId() <= endTimeId)
                .map(TimePoint::getId)
                .collect(Collectors.toList());

        // 检查区间内时间点是否已被占用（此时基础记录已初始化，查询结果准确）
        int unavailableCount = roomReserveDateMapper.countUnavailableTimePoints(
                roomId.intValue(), reservationDate, startTimeId, endTimeId);
        if (unavailableCount > 0) {
            throw new BusinessException("该时间段内部分时间已被预约，请重新选择");
        }

        // 保存预约记录（原有逻辑不变）
        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(request, reservation);
        reservation.setStartTimeId(startTimeId);
        reservation.setEndTimeId(endTimeId);
        reservation.setStatus(0); // 待审核
        reservation.setActivityName(request.getActivityName());
        reservation.setDepartment(request.getDepartment());
        reservation.setUserName(request.getUserName());
        reservation.setCollege(request.getCollege());
        reservation.setMajor(request.getMajor());
        reservation.setContact(request.getContact());
        reservation.setNeedProjection(Boolean.TRUE.equals(request.getNeedProjection()));
        reservation.setTeacherName(request.getTeacherName());
        reservation.setTeacherContact(request.getTeacherContact());
        reservation.setOtherRequirements(request.getOtherRequirements());
        reservation.setAttendees(request.getAttendees());
        reservation.setUserId(openid);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation.setRoomId(request.getRoomId());
        // 生成预约编号（优化：解决SimpleDateFormat线程不安全问题）
        String timeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int randomNum = new Random().nextInt(900) + 100;
        reservation.setReservationNo("R" + timeStr + randomNum);
        save(reservation);

        // ========== 新增修改3：占用区间内所有时间点（更新room_reserve_date表） ==========
        // 批量将区间内时间点标记为「不可用（已预约）」，并关联预约ID/编号
        roomReserveDateMapper.batchUpdateTimePointStatus(
                roomId.intValue(),
                reservationDate,
                rangeTimePointIds,
                0, // status=0：不可用/已预约
                reservation.getId(),
                reservation.getReservationNo()
        );

        // 添加延时任务（原有逻辑不变）
        addReservationCompleteDelayTask(reservation);

        // 返回响应（原有逻辑不变）
        ReservationResponse response = new ReservationResponse();
        BeanUtils.copyProperties(reservation, response);
        return response;
    }

    // 初始化某教室某日期的所有时间点基础记录（确保更新时有记录可更）
    private void initRoomReserveDate(Integer roomId, LocalDate date) {
        // 1. 查询系统中所有时间点
        List<TimePoint> allTimePoints = timePointService.list();
        if (allTimePoints.isEmpty()) {
            throw new BusinessException("系统未配置任何时间点，请先配置");
        }

        // 2. 检查该教室+该日期是否已有时间点记录
        LambdaQueryWrapper<RoomReserveDate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomReserveDate::getRoomId, roomId)
                .eq(RoomReserveDate::getReserveDate, date);
        List<RoomReserveDate> existRecords = roomReserveDateService.list(wrapper);
        Set<Integer> existTimePointIds = existRecords.stream()
                .map(RoomReserveDate::getTimePointId)
                .collect(Collectors.toSet());

        // 3. 筛选出缺失的时间点，批量新增基础记录（status=1 可用）
        List<RoomReserveDate> insertList = allTimePoints.stream()
                .filter(tp -> !existTimePointIds.contains(tp.getId()))
                .map(tp -> {
                    RoomReserveDate record = new RoomReserveDate();
                    record.setRoomId(roomId);
                    record.setReserveDate(date);
                    record.setTimePointId(tp.getId());
                    record.setStatus(1); // 初始状态：可用
                    record.setReservationId(null); // 初始无关联预约
                    record.setReservationNo(null); // 初始无关联预约编号
                    record.setCreatedAt(LocalDateTime.now());
                    record.setUpdatedAt(LocalDateTime.now());
                    return record;
                }).collect(Collectors.toList());

        // 4. 批量插入缺失的记录
        if (!insertList.isEmpty()) {
            roomReserveDateService.saveBatch(insertList);
            log.info("为教室{} {}日期初始化了{}个时间点基础记录", roomId, date, insertList.size());
        }
    }

    /**
     * 添加预约完成延时任务
     */
    private void addReservationCompleteDelayTask(Reservation reservation) {
        try {
            // 获取预约结束时间
            TimePoint endTimePoint = timePointService.getById(reservation.getEndTimeId());
            if (endTimePoint == null) {
                log.error("未找到结束时间点: {}", reservation.getEndTimeId());
                return;
            }

            // 构建预约结束时间（预约日期 + 结束时间点）
            LocalDateTime endDateTime = reservation.getReservationDate()
                    .atTime(endTimePoint.getPoint());

            DelayQueueMessage message = new DelayQueueMessage();
            message.setReservationNo(reservation.getReservationNo());
            message.setReservationId(reservation.getId());
            message.setUserId(reservation.getUserId());
            message.setExecuteTime(endDateTime);
            message.setType("RESERVATION_COMPLETE");

            redisDelayQueueService.addDelayTask(message);
            log.info("预约完成延时任务添加成功: {}, 执行时间: {}",
                    reservation.getReservationNo(), endDateTime);

        } catch (Exception e) {
            log.error("添加预约完成延时任务失败: {}", reservation.getReservationNo(), e);
        }
    }

    /**
     * 完成预约（自动将状态更新为已完成）
     */
    @Override
    @Transactional
    public void completeReservation(String reservationNo) {
        try {


            // 查询预约记录
            Reservation reservation = lambdaQuery()
                    .eq(Reservation::getReservationNo, reservationNo)
                    .one();

            if (reservation == null) {
                return;
            }

            // 只有进行中的预约才能自动完成
//            if (reservation.getStatus() == 1) { // 1表示进行中
            reservation.setStatus(4); // 4表示已完成
            reservation.setUpdatedAt(LocalDateTime.now());

            boolean success = updateById(reservation);
            if (success) {
                log.info("预约自动完成成功: {}", reservationNo);

                // 可以在这里添加完成后的业务逻辑，比如发送通知等
                sendCompletionNotification(reservation);
            } else {
                log.error("预约自动完成失败: {}", reservationNo);
            }
//            } else {
//                log.info("预约状态不是进行中，无需自动完成: {}, 当前状态: {}",
//                        reservationNo, reservation.getStatus());
//            }

        } catch (Exception e) {
            log.error("自动完成预约异常: {}", reservationNo, e);
            throw new BusinessException("自动完成预约失败: " + e.getMessage());
        }
    }

    /**
     * 发送完成通知
     */
    private void sendCompletionNotification(Reservation reservation) {
        // 这里可以实现发送微信模板消息、短信或系统通知
        log.info("发送预约完成通知: {}, 用户: {}",
                reservation.getReservationNo(), reservation.getUserId());

        // 实际实现可以根据业务需求集成消息服务
        // wechatService.sendTemplateMessage(reservation.getUserId(), ...);
    }

    @Override
    public RoomReservationStatusResponse getRoomReservationStatus(Integer roomId, LocalDate date) {
        // 1. 获取教室基本信息
        Room room = roomService.getById(roomId);
        if (room == null) {
            throw new BusinessException("教室不存在");
        }

        // ========== 核心修改：从room_reserve_date查询已占用的时间点 ==========
        // 查询该教室+日期下所有被占用的时间点（status=0）
        List<RoomReserveDate> occupiedTimePoints = roomReserveDateMapper.selectList(
                new LambdaQueryWrapper<RoomReserveDate>()
                        .eq(RoomReserveDate::getRoomId, roomId)
                        .eq(RoomReserveDate::getReserveDate, date)
                        .eq(RoomReserveDate::getStatus, 0) // 0=已占用
        );

        // 按预约编号分组，组装时间段（一个预约对应多个时间点）
        Map<String, List<RoomReserveDate>> reservationGroup = occupiedTimePoints.stream()
                .filter(otp -> StringUtils.isNotBlank(otp.getReservationNo()))
                .collect(Collectors.groupingBy(RoomReserveDate::getReservationNo));

        // 组装已预约时间段
        List<TimeRangeDto> reservedRanges = new ArrayList<>();
        for (Map.Entry<String, List<RoomReserveDate>> entry : reservationGroup.entrySet()) {
            String reservationNo = entry.getKey();
            List<RoomReserveDate> timePoints = entry.getValue();

            // 按时间点ID排序，获取开始/结束时间点
            List<Integer> timePointIds = timePoints.stream()
                    .map(RoomReserveDate::getTimePointId)
                    .sorted()
                    .collect(Collectors.toList());

            if (timePointIds.size() < 1) {
                continue;
            }

            // 获取开始/结束时间点的具体时间
            TimePoint startPoint = timePointService.getById(timePointIds.get(0));
            TimePoint endPoint = timePointService.getById(timePointIds.get(timePointIds.size() - 1));

            if (startPoint == null || endPoint == null) {
                continue;
            }

            // 补充预约记录的用户ID（从reservations表查询）
            Reservation reservation = lambdaQuery()
                    .eq(Reservation::getReservationNo, reservationNo)
                    .one();

            TimeRangeDto dto = new TimeRangeDto();
            dto.setStart(startPoint.getPoint().toString());
            dto.setEnd(endPoint.getPoint().toString());
            dto.setReservationNo(reservationNo);
            dto.setUserId(reservation != null ? reservation.getUserId() : "");
            reservedRanges.add(dto);
        }

        // 4. 构建响应对象（原有逻辑不变）
        RoomReservationStatusResponse response = new RoomReservationStatusResponse();
        response.setRoomId(roomId);
        response.setRoomName(room.getName());
        response.setDate(date);
        response.setReservedTimeRanges(reservedRanges);

        return response;
    }

    /**
     * 核心方法：检查区间内是否有时间冲突
     */
    private boolean hasTimeConflict(Long roomId, LocalDate reservationDate, Integer startPointId, Integer endPointId) {
        // ========== 替换原有逻辑 ==========
        int unavailableCount = roomReserveDateMapper.countUnavailableTimePoints(
                roomId.intValue(), reservationDate, startPointId, endPointId);
        return unavailableCount > 0;

        // 注释掉原有逻辑
        // LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<Reservation>()
        //         .eq(Reservation::getRoomId, roomId)
        //         .eq(Reservation::getReservationDate, reservationDate)
        //         .le(Reservation::getStartTimeId, endPointId)
        //         .ge(Reservation::getEndTimeId, startPointId)
        //         .in(Reservation::getStatus, 0, 1);
        // return count(queryWrapper) > 0;
    }
    /**
     * 根据用户ID查询预约记录
     */
    @Override
    public ReservationVO getReservationDetail(Integer id) {
        // 1. 直接查询数据库（只查一条）
        Reservation reservation = reservationMapper.selectById(id);

        // 如果没查到，返回 null
        if (reservation == null) {
            return null;
        }

        // 2. 转换为 VO (复用之前的逻辑，填充额外信息)
        ReservationVO vo = new ReservationVO();
        vo.setId(reservation.getId());
        vo.setReservationNo(reservation.getReservationNo());
        vo.setRoomId(reservation.getRoomId());
        vo.setReservationDate(reservation.getReservationDate());
        vo.setActivityName(reservation.getActivityName());
        vo.setStatus(reservation.getStatus());
        vo.setAttendees(reservation.getAttendees());
        vo.setCreatedAt(reservation.getCreatedAt());
        vo.setDepartment(reservation.getDepartment());
        vo.setUserName(reservation.getUserName());
        vo.setCollege(reservation.getCollege());
        vo.setMajor(reservation.getMajor());
        vo.setContact(reservation.getContact());
        vo.setTeacherName(reservation.getTeacherName());
        vo.setTeacherContact(reservation.getTeacherContact());
        vo.setOtherRequirements(reservation.getOtherRequirements());


        try {
            // 3. 填充关联信息 (防止空指针)
            // 获取时间点信息
            TimePointVO startTime = timePointService.getTimeSlotById(reservation.getStartTimeId());
            TimePointVO endTime = timePointService.getTimeSlotById(reservation.getEndTimeId());
            if (startTime != null) vo.setStartTime(startTime.getPoint().toString());
            if (endTime != null) vo.setEndTime(endTime.getPoint().toString());

            // 获得教室名称
            RoomResponse room = roomService.getRoomById(reservation.getRoomId());
            if (room != null) vo.setRoomName(room.getName());

            // 获得社区名称 (假设 RoomResponse 里有 communityName)
            if (room != null) vo.setCommunityName(room.getCommunityName());

            // 获得预约审核信息
            String statusDescription = reservationApprovalService.getStatusDescription(reservation.getStatus());
            vo.setStatusDesc(statusDescription);

            // 处理审核意见
            String auditReason = reservationApprovalService.getLatestReasonByReservationId(reservation.getId());
            vo.setAuditReason(auditReason);
            vo.setAuditTime(reservation.getAuditTime());

        } catch (Exception e) {
            log.error("填充预约详情额外信息失败: {}", e.getMessage());
            // 即使关联信息失败，也返回基础信息
        }

        return vo;
    }

    /**
     * 取消预约
     */
    @Override
    @Transactional
    public void cancelReservation(String reservationNo, String userId) {
        // 1. 查询预约记录（原有逻辑）
        Reservation reservation = reservationMapper.selectByReservationNo(reservationNo)
                .orElseThrow(() -> new BusinessException("预约记录不存在"));

        // 2. 检查预约状态（原有逻辑）
        if (reservation.getStatus() != 0 && reservation.getStatus() != 1) {
            throw new BusinessException("当前状态不可取消");
        }

        // 3. 检查取消时间（原有逻辑）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancelDeadline = reservation.getReservationDate().atTime(
                getTimeById(reservation.getStartTimeId()).minusHours(1));
        if (now.isAfter(cancelDeadline)) {
            throw new BusinessException("已超过可取消时间（需在预约开始前1小时取消）");
        }

        // ========== 新增修改：释放区间内所有时间点 ==========
        // 查询系统中所有时间点，解析出预约区间内的所有时间点ID
        List<TimePoint> allSystemTimePoints = timePointService.list();
        allSystemTimePoints.sort(Comparator.comparing(TimePoint::getPoint));
        List<Integer> rangeTimePointIds = allSystemTimePoints.stream()
                .filter(tp -> tp.getId() >= reservation.getStartTimeId()
                        && tp.getId() <= reservation.getEndTimeId())
                .map(TimePoint::getId)
                .collect(Collectors.toList());
        // 批量将区间内时间点标记为「可用（未预约）」，清空预约关联
        roomReserveDateMapper.batchUpdateTimePointStatus(
                reservation.getRoomId().intValue(),
                reservation.getReservationDate(),
                rangeTimePointIds,
                1, // status=1：可用/未预约
                null, // 清空预约ID
                null  // 清空预约编号
        );

        // 4. 更新状态为已取消（原有逻辑）
        reservation.setStatus(3);
        reservationMapper.updateById(reservation);

        // 5. 移除延时任务（原有逻辑）
        DelayQueueMessage message = new DelayQueueMessage();
        message.setReservationNo(reservationNo);
        message.setReservationId(reservation.getId());
        message.setUserId(userId);
        message.setType("RESERVATION_COMPLETE");
        // redisDelayQueueService.removeDelayTask(message);
        log.info("取消预约时移除延时任务: {}", reservationNo);

        // 6. 判断当月取消次数（原有逻辑）
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        int cnt = reservationMapper.countByUserIdAndStatusAndCancelTimeAfter(
                userId, 3, oneMonthAgo);
        if (cnt >= 2) {
            LocalDateTime penaltyStart = LocalDateTime.now();
            LocalDateTime penaltyEnd   = penaltyStart.plusMonths(3);
            penaltyService.createPenalty(userId, penaltyStart, penaltyEnd);
        }
    }

    /**
     * 重新提交被退回的预约
     */
    @Override
    @Transactional
    public void resubmitReservation(Integer reservationId, String userId) {
        try {
            log.info("重新提交预约: reservationId={}, userId={}", reservationId, userId);

            // 1. 查询预约记录
            Reservation reservation = getById(reservationId);
            if (reservation == null) {
                throw new BusinessException("预约记录不存在");
            }

            // 2. 验证用户权限
            if (!reservation.getUserId().equals(userId)) {
                throw new BusinessException("无权操作此预约");
            }

            // 3. 检查预约状态是否为被拒绝（2）
            if (reservation.getStatus() != 2) {
                throw new BusinessException("只有被拒绝的预约才能重新提交");
            }

            // 4. 检查时间冲突
            TimePoint startPoint = timePointService.getById(reservation.getStartTimeId());
            TimePoint endPoint = timePointService.getById(reservation.getEndTimeId());

            if (startPoint == null || endPoint == null) {
                throw new BusinessException("时间点数据异常");
            }

            if (hasTimeConflict(reservation.getRoomId(), reservation.getReservationDate(),
                    reservation.getStartTimeId(), reservation.getEndTimeId())) {
                throw new BusinessException("该时间段已被其他活动预定，无法重新提交");
            }

            // 5. 更新状态为待审核（0）
            reservation.setStatus(0);
            reservation.setUpdatedAt(LocalDateTime.now());

            boolean updateSuccess = updateById(reservation);
            if (!updateSuccess) {
                throw new BusinessException("重新提交预约失败");
            }

            // 6. 重新添加延时任务
            addReservationCompleteDelayTask(reservation);

            log.info("重新提交预约成功: reservationId={}", reservationId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("重新提交预约失败: reservationId={}, userId={}", reservationId, userId, e);
            throw new BusinessException("重新提交预约失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户最近3次预约记录
     */
    @Override
    public List<ReservationVO> getLatestReservations(String userId) {
        // 1. 查询用户最近的3条预约记录，按创建时间降序排列
        List<Reservation> reservations = lambdaQuery()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getCreatedAt)
                .last("LIMIT 3")
                .list();

        // 2. 转换为VO对象
        return reservations.stream()
                .map(reservation -> {
                    ReservationVO vo = new ReservationVO();
                    BeanUtils.copyProperties(reservation, vo);

                    // 获取时间点信息
                    TimePointVO startTime = timePointService.getTimeSlotById(reservation.getStartTimeId());
                    TimePointVO endTime = timePointService.getTimeSlotById(reservation.getEndTimeId());

                    // 获取教室信息
                    RoomResponse room = roomService.getRoomById(reservation.getRoomId());

                    // 设置额外信息
                    vo.setRoomName(room.getName());
                    vo.setStartTime(startTime.getPoint().toString());
                    vo.setEndTime(endTime.getPoint().toString());

                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据 timePointId 拿到对应的 LocalTime。
     */
    private LocalTime getTimeById(Integer timePointId) {
        TimePoint tp = timePointService.getById(timePointId);
        if (tp == null) {
            throw new BusinessException("时间点不存在，id=" + timePointId);
        }
        return tp.getPoint();
    }
    @Override
    public List<ReservationVO> getReservationsByUserId(String userId, Integer status) {

        // 1. 查询数据库实体
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId);
        if (status != null) {
            queryWrapper.eq(Reservation::getStatus, status);
        }
        queryWrapper.orderByDesc(Reservation::getCreatedAt);
        List<Reservation> entityList = reservationMapper.selectList(queryWrapper);

        // 2. 转换为 VO
        return entityList.stream().map(reservation -> {
            ReservationVO vo = new ReservationVO();

            // === A. 基础字段直接复制 ===
            // 建议使用 BeanUtils.copyProperties(reservation, vo); 可以省去很多 set 代码
            // 或者手动 set 如下：
            vo.setId(reservation.getId()); // 注意类型 Long/Integer 是否匹配
            vo.setReservationNo(reservation.getReservationNo());
            vo.setRoomId(reservation.getRoomId()); // 补上
            vo.setReservationDate(reservation.getReservationDate());
            vo.setActivityName(reservation.getActivityName());
            vo.setStatus(reservation.getStatus());
            vo.setAttendees(reservation.getAttendees()); // 补上
            vo.setCreatedAt(reservation.getCreatedAt()); // 补上
            vo.setDepartment(reservation.getDepartment()); // 补上
            vo.setUserName(reservation.getUserName()); // 补上
            vo.setCollege(reservation.getCollege()); // 补上
            vo.setMajor(reservation.getMajor()); // 补上
            vo.setContact(reservation.getContact()); // 补上
            vo.setTeacherName(reservation.getTeacherName()); // 补上
            vo.setTeacherContact(reservation.getTeacherContact()); // 补上
            vo.setOtherRequirements(reservation.getOtherRequirements()); // 补上
            vo.setAuditTime(reservation.getAuditTime()); // 补上

            // === B. 关联信息查询与转换 ===

            // 1. 教室名称 & 社区名称
            try {
                RoomResponse room = roomService.getRoomById(reservation.getRoomId());
                if (room != null) {
                    vo.setRoomName(room.getName());
                    vo.setCommunityName(room.getCommunityName());
                }
            } catch (Exception e) {
                // 忽略关联查询失败
            }

            // 2. 时间段转换 (ID -> "08:00")
            try {
                TimePointVO startTp = timePointService.getTimeSlotById(reservation.getStartTimeId());
                if (startTp != null) vo.setStartTime(startTp.getPoint().toString());

                TimePointVO endTp = timePointService.getTimeSlotById(reservation.getEndTimeId());
                if (endTp != null) vo.setEndTime(endTp.getPoint().toString());
            } catch (Exception e) {
                // 忽略
            }

            // 3. 状态描述
            // vo.setStatusDesc(getStatusDesc(reservation.getStatus())); // 如果有这种辅助方法

            return vo;
        }).collect(Collectors.toList());
    }
}
