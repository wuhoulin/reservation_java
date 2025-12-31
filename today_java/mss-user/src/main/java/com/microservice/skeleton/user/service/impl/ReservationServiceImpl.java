package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.Request.CheckInRequest;
import com.microservice.skeleton.user.domain.Request.ReservationRequest;
import com.microservice.skeleton.user.domain.Response.ReservationResponse;
import com.microservice.skeleton.user.domain.Response.RoomReservationStatusResponse;
import com.microservice.skeleton.user.domain.Response.RoomResponse;
import com.microservice.skeleton.user.domain.dto.TimeRangeDto;
import com.microservice.skeleton.user.domain.entity.*;
import com.microservice.skeleton.user.domain.vo.CheckInStateVO;
import com.microservice.skeleton.user.domain.vo.ReservationVO;
import com.microservice.skeleton.user.domain.vo.TimePointVO;
import com.microservice.skeleton.user.mapper.ReservationMapper;
import com.microservice.skeleton.user.mapper.RoomMapper;
import com.microservice.skeleton.user.mapper.RoomReserveDateMapper;
import com.microservice.skeleton.user.mapper.TimePointMapper;
import com.microservice.skeleton.user.service.*;
import com.microservice.skeleton.user.util.DistanceUtil;
import com.microservice.skeleton.user.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private TimePointService timePointService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private UserPenaltyService penaltyService;
    @Autowired
    @Lazy
    private RedisDelayQueueService redisDelayQueueService;
    @Autowired
    private RoomReserveDateMapper roomReserveDateMapper;
    @Autowired
    private RoomReserveDateService roomReserveDateService;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private TimePointMapper timePointMapper;

    private static final double ALLOWED_DISTANCE = 200.0;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse createReservation(ReservationRequest request) {
        // 获取当前用户
        String openid = UserContext.getCurrentOpenid();
        if (openid == null || openid.trim().isEmpty()) {
            throw new BusinessException("用户未登录");
        }

        // 校验时间参数
        List<Integer> timePointIds = request.getTimePointIds();
        if (timePointIds == null || timePointIds.size() < 2) {
            throw new BusinessException("至少选择2个时间点");
        }

        // 获取并排序时间点
        List<TimePoint> timePoints = timePointService.listByIds(timePointIds);
        if (timePoints.size() != timePointIds.size()) {
            throw new BusinessException("时间点数据异常");
        }
        timePoints.sort(Comparator.comparing(TimePoint::getPoint));

        TimePoint startPoint = timePoints.get(0);
        TimePoint endPoint = timePoints.get(timePoints.size() - 1);

        if (startPoint.getPoint().equals(endPoint.getPoint())) {
            throw new BusinessException("起止时间不能相同");
        }

        LocalDate reservationDate = request.getReservationDate();
        Long roomId = request.getRoomId();
        Integer startTimeId = startPoint.getId();
        Integer endTimeId = endPoint.getId();

        // 解决并发问题：使用 synchronized 锁住 "房间+日期"
        // 注意：单体应用有效，若是微服务集群建议使用 Redis 分布式锁
        String lockKey = ("room-" + roomId + "-" + reservationDate).intern();

        Reservation reservation = new Reservation();

        synchronized (lockKey) {
            // 初始化库存记录
            initRoomReserveDate(roomId.intValue(), reservationDate);

            // 二次检查冲突
            int unavailableCount = roomReserveDateMapper.countUnavailableTimePoints(
                    roomId.intValue(), reservationDate, startTimeId, endTimeId);
            if (unavailableCount > 0) {
                throw new BusinessException("手慢了，该时间段刚被抢占");
            }

            // 组装预约对象
            BeanUtils.copyProperties(request, reservation);
            reservation.setStartTimeId(startTimeId);
            reservation.setEndTimeId(endTimeId);

            // 核心修改：状态直接设为 1 (已通过)
            reservation.setStatus(1);

            // 补充详情
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

            // 设置时间
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setUpdatedAt(LocalDateTime.now());
            reservation.setAuditTime(LocalDateTime.now()); // 自动审核时间
            reservation.setRoomId(request.getRoomId());
            reservation.setRemark("系统自动通过");

            // 生成单号
            String timeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            int randomNum = new Random().nextInt(900) + 100;
            reservation.setReservationNo("R" + timeStr + randomNum);

            // 保存预约
            save(reservation);

            // 立即锁定库存 (将 room_reserve_date 对应时段置为不可用)
            // 1. 计算涉及的所有时间点ID
            List<TimePoint> allPoints = timePointService.list();
            List<Integer> rangeIds = allPoints.stream()
                    .filter(tp -> tp.getId() >= startTimeId && tp.getId() <= endTimeId)
                    .map(TimePoint::getId)
                    .collect(Collectors.toList());

            // 2. 执行锁定 (status=0 代表占用)
            if (!rangeIds.isEmpty()) {
                roomReserveDateMapper.batchUpdateTimePointStatus(
                        roomId.intValue(),
                        reservationDate,
                        rangeIds,
                        0,
                        reservation.getId(),
                        reservation.getReservationNo()
                );
            }
        }

        // 添加延时任务
        addReservationCompleteDelayTask(reservation);

        // 返回结果
        ReservationResponse response = new ReservationResponse();
        BeanUtils.copyProperties(reservation, response);
        return response;
    }

    private void initRoomReserveDate(Integer roomId, LocalDate date) {
        List<TimePoint> allTimePoints = timePointService.list();
        if (allTimePoints.isEmpty()) return;

        LambdaQueryWrapper<RoomReserveDate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomReserveDate::getRoomId, roomId).eq(RoomReserveDate::getReserveDate, date);
        List<RoomReserveDate> existRecords = roomReserveDateService.list(wrapper);
        Set<Integer> existTimePointIds = existRecords.stream()
                .map(RoomReserveDate::getTimePointId).collect(Collectors.toSet());

        List<RoomReserveDate> insertList = allTimePoints.stream()
                .filter(tp -> !existTimePointIds.contains(tp.getId()))
                .map(tp -> {
                    RoomReserveDate record = new RoomReserveDate();
                    record.setRoomId(roomId);
                    record.setReserveDate(date);
                    record.setTimePointId(tp.getId());
                    record.setStatus(1);
                    record.setCreatedAt(LocalDateTime.now());
                    record.setUpdatedAt(LocalDateTime.now());
                    return record;
                }).collect(Collectors.toList());

        if (!insertList.isEmpty()) {
            roomReserveDateService.saveBatch(insertList);
        }
    }

    private void addReservationCompleteDelayTask(Reservation reservation) {
        try {
            TimePoint endTimePoint = timePointService.getById(reservation.getEndTimeId());
            if (endTimePoint == null) return;

            LocalDateTime endDateTime = reservation.getReservationDate().atTime(endTimePoint.getPoint());
            DelayQueueMessage message = new DelayQueueMessage();
            message.setReservationNo(reservation.getReservationNo());
            message.setReservationId(reservation.getId());
            message.setUserId(reservation.getUserId());
            message.setExecuteTime(endDateTime);
            message.setType("RESERVATION_COMPLETE");

            redisDelayQueueService.addDelayTask(message);
        } catch (Exception e) {
            log.error("添加延时任务失败", e);
        }
    }

    @Override
    @Transactional
    public void completeReservation(String reservationNo) {
        try {
            Reservation reservation = lambdaQuery()
                    .eq(Reservation::getReservationNo, reservationNo)
                    .one();

            if (reservation == null) return;

            Integer currentStatus = reservation.getStatus();

            // 待审核 -> 已过期
            if (currentStatus == 0) {
                reservation.setStatus(5);
                reservation.setUpdatedAt(LocalDateTime.now());
                updateById(reservation);
                log.info("预约超时未审核，自动过期: {}", reservationNo);
            }
            // 已通过 -> 已完成
            else if (currentStatus == 1) {
                reservation.setStatus(4);
                reservation.setUpdatedAt(LocalDateTime.now());
                updateById(reservation);
                log.info("预约时间结束，自动完成: {}", reservationNo);
                sendCompletionNotification(reservation);
            }

        } catch (Exception e) {
            log.error("自动处理预约状态异常", e);
        }
    }

    private void sendCompletionNotification(Reservation reservation) {
        log.info("发送预约完成通知: {}", reservation.getReservationNo());
    }

    @Override
    public RoomReservationStatusResponse getRoomReservationStatus(Integer roomId, LocalDate date) {
        Room room = roomService.getById(roomId);
        if (room == null) throw new BusinessException("教室不存在");

        List<RoomReserveDate> occupiedTimePoints = roomReserveDateMapper.selectList(
                new LambdaQueryWrapper<RoomReserveDate>()
                        .eq(RoomReserveDate::getRoomId, roomId)
                        .eq(RoomReserveDate::getReserveDate, date)
                        .eq(RoomReserveDate::getStatus, 0)
        );

        Map<String, List<RoomReserveDate>> reservationGroup = occupiedTimePoints.stream()
                .filter(otp -> StringUtils.isNotBlank(otp.getReservationNo()))
                .collect(Collectors.groupingBy(RoomReserveDate::getReservationNo));

        List<TimeRangeDto> reservedRanges = new ArrayList<>();
        for (Map.Entry<String, List<RoomReserveDate>> entry : reservationGroup.entrySet()) {
            String reservationNo = entry.getKey();
            List<RoomReserveDate> timePoints = entry.getValue();
            List<Integer> timePointIds = timePoints.stream()
                    .map(RoomReserveDate::getTimePointId).sorted().collect(Collectors.toList());

            if (timePointIds.isEmpty()) continue;

            TimePoint startPoint = timePointService.getById(timePointIds.get(0));
            TimePoint endPoint = timePointService.getById(timePointIds.get(timePointIds.size() - 1));

            if (startPoint == null || endPoint == null) continue;

            Reservation reservation = lambdaQuery()
                    .eq(Reservation::getReservationNo, reservationNo).one();

            TimeRangeDto dto = new TimeRangeDto();
            dto.setStart(startPoint.getPoint().toString());
            dto.setEnd(endPoint.getPoint().toString());
            dto.setReservationNo(reservationNo);
            dto.setUserId(reservation != null ? reservation.getUserId() : "");
            reservedRanges.add(dto);
        }

        RoomReservationStatusResponse response = new RoomReservationStatusResponse();
        response.setRoomId(roomId);
        response.setRoomName(room.getName());
        response.setDate(date);
        response.setReservedTimeRanges(reservedRanges);
        return response;
    }

    private boolean hasTimeConflict(Long roomId, LocalDate reservationDate, Integer startPointId, Integer endPointId) {
        int unavailableCount = roomReserveDateMapper.countUnavailableTimePoints(
                roomId.intValue(), reservationDate, startPointId, endPointId);
        return unavailableCount > 0;
    }

    @Override
    public ReservationVO getReservationDetail(Integer id) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) return null;

        // 使用 convertToVO 统一转换逻辑，确保包含教室名称和时间字符串
        return convertToVO(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(String reservationNo, String userId) {
        Reservation reservation = reservationMapper.selectByReservationNo(reservationNo)
                .orElseThrow(() -> new BusinessException("预约不存在"));

        if (reservation.getStatus() != 0 && reservation.getStatus() != 1) {
            throw new BusinessException("当前状态不可取消");
        }

        // 修改点：如果是已通过(status=1)的预约，检查取消截止时间
        if (reservation.getStatus() == 1) {
            LocalDateTime now = LocalDateTime.now();

            // 🔥🔥🔥 核心修改：minusHours(1) -> minusHours(3) 🔥🔥🔥
            // 截止时间 = 预约开始时间 - 3小时
            LocalDateTime cancelDeadline = reservation.getReservationDate().atTime(
                    getTimeById(reservation.getStartTimeId()).minusHours(3));

            if (now.isAfter(cancelDeadline)) {
                throw new BusinessException("已通过的预约需在开始前3小时取消"); // 提示文案也同步修改
            }
        }

        // ... (后续释放库存、更新状态、惩罚逻辑保持不变) ...
        List<TimePoint> allPoints = timePointService.list();
        allPoints.sort(Comparator.comparing(TimePoint::getPoint));
        List<Integer> rangeIds = allPoints.stream()
                .filter(tp -> tp.getId() >= reservation.getStartTimeId() && tp.getId() <= reservation.getEndTimeId())
                .map(TimePoint::getId).collect(Collectors.toList());

        roomReserveDateMapper.batchUpdateTimePointStatus(
                reservation.getRoomId().intValue(),
                reservation.getReservationDate(),
                rangeIds, 1, null, null
        );

        reservation.setStatus(3); // 已取消
        reservationMapper.updateById(reservation);

        // 统计取消次数进行惩罚
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        int cnt = reservationMapper.countByUserIdAndStatusAndCancelTimeAfter(userId, 3, oneMonthAgo);
        if (cnt >= 2) {
            penaltyService.createPenalty(userId, LocalDateTime.now(), LocalDateTime.now().plusMonths(3));
        }
    }

    @Override
    @Transactional
    public void resubmitReservation(Integer reservationId, String userId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) throw new BusinessException("预约不存在");
        if (!reservation.getUserId().equals(userId)) throw new BusinessException("无权操作");
        if (reservation.getStatus() != 2) throw new BusinessException("仅被拒绝的预约可重新提交");

        if (hasTimeConflict(reservation.getRoomId(), reservation.getReservationDate(),
                reservation.getStartTimeId(), reservation.getEndTimeId())) {
            throw new BusinessException("时间段已被占用");
        }

        reservation.setStatus(0);
        reservation.setUpdatedAt(LocalDateTime.now());
        updateById(reservation);
        addReservationCompleteDelayTask(reservation);
    }

    @Override
    public List<ReservationVO> getLatestReservations(String userId) {
        List<Reservation> reservations = lambdaQuery()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getCreatedAt)
                .last("LIMIT 3").list();

        return reservations.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private LocalTime getTimeById(Integer timePointId) {
        TimePoint tp = timePointService.getById(timePointId);
        if (tp == null) throw new BusinessException("时间点不存在");
        return tp.getPoint();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void performCheckIn(String userId, CheckInRequest request) {
        // 1. 基础校验
        Reservation reservation = reservationMapper.selectById(request.getReservationId());
        if (reservation == null) throw new RuntimeException("预约不存在");
        if (reservation.getCheckInStatus() == 1) throw new RuntimeException("已签到");
        if (reservation.getStatus() != 1) throw new RuntimeException("当前状态不可签到");

        // 2. 获取当前时间、开始时间、结束时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = getRealTime(reservation.getReservationDate(), reservation.getStartTimeId());
        LocalDateTime endTime = getRealTime(reservation.getReservationDate(), reservation.getEndTimeId());

        // 3. 修改时间判断逻辑
        // 逻辑：必须在 [开始前15分钟] 之后，且在 [结束时间] 之前

        // A. 检查是否太早 (早于开始前15分钟)
        if (now.isBefore(startTime.minusMinutes(15))) {
            throw new RuntimeException("活动未开始，请在开始前15分钟内签到");
        }

        // B. 检查是否太晚 (晚于结束时间) - 原代码是 diff > 30，这里改成了晚于 endTime
        if (now.isAfter(endTime)) {
            throw new RuntimeException("活动已结束，签到通道已关闭");
        }

        // 4. 距离校验 (保持不变)
        Room room = roomMapper.selectById(reservation.getRoomId());
        double dist = DistanceUtil.getDistance(
                request.getLatitude().doubleValue(), request.getLongitude().doubleValue(),
                room.getLatitude().doubleValue(), room.getLongitude().doubleValue());

        if (dist > ALLOWED_DISTANCE) throw new RuntimeException("距离过远，请到现场签到");

        // 5. 更新状态
        reservation.setCheckInStatus(1);
        reservation.setCheckInTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);
    }

    private LocalDateTime getRealTime(LocalDate date, Integer timePointId) {
        TimePoint tp = timePointMapper.selectById(timePointId);
        if (tp == null) throw new RuntimeException("时间配置错误");
        return LocalDateTime.of(date, tp.getPoint());
    }

    private LocalDateTime getRealStartTime(LocalDate date, Integer startId) {
        TimePoint tp = timePointMapper.selectById(startId);
        if (tp == null) throw new RuntimeException("时间配置错误");
        return LocalDateTime.of(date, tp.getPoint());
    }

    public ReservationVO findCurrentCheckInTask(String openid) {
        List<Reservation> list = reservationMapper.selectList(new QueryWrapper<Reservation>()
                .eq("user_id", openid).eq("status", 1).eq("check_in_status", 0));

        LocalDateTime now = LocalDateTime.now();
        for (Reservation res : list) {
            LocalDateTime start = getRealStartTime(res.getReservationDate(), res.getStartTimeId());
            long diff = ChronoUnit.MINUTES.between(start, now);
            if (diff >= -30 && diff <= 30) {
                return convertToVO(res);
            }
        }
        return null;
    }

    @Override
    public CheckInStateVO getCheckInState(String openid) {
        CheckInStateVO result = new CheckInStateVO();
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> list = reservationMapper.selectList(new QueryWrapper<Reservation>()
                .eq("user_id", openid)
                .eq("status", 1)
                .eq("check_in_status", 0)
                .orderByAsc("reservation_date")
                .orderByAsc("start_time_id"));

        if (list.isEmpty()) {
            result.setState(0);
            return result;
        }

        Reservation target = null;
        int state = 0;

        for (Reservation res : list) {
            LocalDateTime start = getRealStartTime(res.getReservationDate(), res.getStartTimeId());
            long diff = ChronoUnit.MINUTES.between(start, now);

            if (diff >= -30 && diff <= 30) {
                target = res;
                state = 1;
                break;
            }
            if (diff < -30) {
                target = res;
                state = 2;
                break;
            }
        }

        if (target == null) {
            result.setState(0);
            return result;
        }

        result.setState(state);
        // 这里 convertToVO 修复了，会正确填充地点和时间
        result.setTaskInfo(convertToVO(target));

        if (state == 1) {
            result.setMessage("当前活动进行中，请签到");
        } else {
            result.setMessage("活动尚未开始");
            LocalDateTime start = getRealStartTime(target.getReservationDate(), target.getStartTimeId());
            LocalDateTime checkInTime = start.minusMinutes(30);
            long ms = java.time.Duration.between(now, checkInTime).toMillis();
            result.setCountdownMs(ms > 0 ? ms : 0);
        }
        return result;
    }

    /**
     * VO转换核心方法
     * 修复：从rooms表获取地点，从time_points表获取具体时间字符串
     * 修改：审核意见直接从 reservations 表的 remark 字段获取
     */
    private ReservationVO convertToVO(Reservation reservation) {
        if (reservation == null) return null;
        ReservationVO vo = new ReservationVO();
        BeanUtils.copyProperties(reservation, vo);

        // 1. 填充地点信息 (来自 rooms 表)
        Room room = roomMapper.selectById(reservation.getRoomId());
        if (room != null) {
            vo.setRoomName(room.getName());
            vo.setLongitude(room.getLongitude());
            vo.setLatitude(room.getLatitude());
            vo.setCommunityName(room.getName());
        }

        // 2. 填充开始时间字符串 (来自 time_points 表)
        TimePoint startTp = timePointMapper.selectById(reservation.getStartTimeId());
        if (startTp != null) {
            vo.setStartTime(startTp.getPoint().toString()); // 例如 "08:30"
        }

        // 3. 填充结束时间字符串
        TimePoint endTp = timePointMapper.selectById(reservation.getEndTimeId());
        if (endTp != null) {
            vo.setEndTime(endTp.getPoint().toString());
        }

        // 4. 填充审核信息
        if (reservation.getStatus() != null) {
            String statusDesc = getStatusDescription(reservation.getStatus());
            vo.setStatusDesc(statusDesc);
            // 关键修改：直接用 reservations 表的 remark
            vo.setAuditReason(reservation.getRemark());
            vo.setAuditTime(reservation.getAuditTime());
        }

        return vo;
    }

    // 简单的状态描述映射 (替代 ReservationApprovalService)
    private String getStatusDescription(Integer status) {
        switch (status) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已拒绝";
            case 3: return "已取消";
            case 4: return "已完成";
            case 5: return "已过期";
            default: return "未知状态";
        }
    }

    @Override
    public Map<Integer, Integer> getPendingCounts(Integer roomId, LocalDate date) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getRoomId, roomId)
                .eq(Reservation::getReservationDate, date)
                .eq(Reservation::getStatus, 0);
        List<Reservation> list = list(wrapper);

        Map<Integer, Integer> countMap = new HashMap<>();
        List<TimePoint> allPoints = timePointService.list();
        for (Reservation res : list) {
            for (TimePoint tp : allPoints) {
                if (tp.getId() >= res.getStartTimeId() && tp.getId() <= res.getEndTimeId()) {
                    countMap.merge(tp.getId(), 1, Integer::sum);
                }
            }
        }
        return countMap;
    }

    @Override
    public List<ReservationVO> getReservationsByUserId(String userId, Integer status, Integer roomId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getUserId, userId);
        if (status != null) wrapper.eq(Reservation::getStatus, status);
        if (roomId != null) wrapper.eq(Reservation::getRoomId, roomId);
        wrapper.orderByDesc(Reservation::getCreatedAt);

        return list(wrapper).stream().map(this::convertToVO).collect(Collectors.toList());
    }
}
