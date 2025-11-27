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
import com.microservice.skeleton.user.domain.entity.Reservation;
import com.microservice.skeleton.user.domain.entity.Room;
import com.microservice.skeleton.user.domain.entity.TimePoint;
import com.microservice.skeleton.user.domain.vo.ReservationVO;
import com.microservice.skeleton.user.domain.vo.TimePointVO;
import com.microservice.skeleton.user.mapper.ReservationMapper;
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

    @Override
    @Transactional // 确保事务生效
    public ReservationResponse createReservation(ReservationRequest request) {
        String openid = UserContext.getCurrentOpenid();
        List<Integer> timePointIds = request.getTimePointIds();

        // 1. 校验时间点合法性（非空、至少2个）
        if (timePointIds == null || timePointIds.size() < 2) {
            throw new BusinessException("至少选择2个时间点（开始和结束）");
        }

        // 2. 获取所有时间点数据，并按时间排序（确保 startTime < endTime）
        List<TimePoint> timePoints = timePointService.listByIds(timePointIds);
        if (timePoints.size() != timePointIds.size()) {
            throw new BusinessException("部分时间点数据不存在");
        }

        // 按时间点排序（例如：09:00 → 10:00 → 11:00）
        timePoints.sort(Comparator.comparing(TimePoint::getPoint));
        TimePoint startPoint = timePoints.get(0);
        TimePoint endPoint = timePoints.get(timePoints.size() - 1);


        if (startPoint.getPoint().equals(endPoint.getPoint())) {
            throw new BusinessException("开始时间和结束时间不能相同");
        }

        LocalDate reservationDate = request.getReservationDate();
        Integer roomId = request.getRoomId();
        Integer startTimeId = startPoint.getId();
        Integer endTimeId = endPoint.getId();


        List<Reservation> overlappingReservations = reservationMapper.findOverlappingReservationsForUpdate(
                roomId, reservationDate, startTimeId, endTimeId
        );

        if (!overlappingReservations.isEmpty()) {
            // 如果查到了记录，说明有冲突，抛出异常。
            // 此时，锁在事务结束前依然持有。
            throw new BusinessException("该时间段内部分时间已被预约，请重新选择");
        }

        // 5. 保存预约记录
        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(request, reservation);
        reservation.setStartTimeId(startTimeId); // 区间开始时间点ID
        reservation.setEndTimeId(endTimeId);     // 区间结束时间点ID
        reservation.setStatus(0); // 待审核

        // 设置其他字段
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

        // 生成预约编号（保持原有逻辑）
        String timeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int randomNum = new Random().nextInt(900) + 100;
        reservation.setReservationNo("R" + timeStr + randomNum);


        save(reservation);


        // 返回响应
        ReservationResponse response = new ReservationResponse();
        BeanUtils.copyProperties(reservation, response);
        return response;
    }
    @Override
    public RoomReservationStatusResponse getRoomReservationStatus(Integer roomId, LocalDate date) {
        // 1. 获取教室基本信息
        Room room = roomService.getById(roomId);
        if (room == null) {
            throw new BusinessException("教室不存在");
        }

        // 2. 查询该教室当天的所有预约记录
        List<Reservation> reservations = lambdaQuery()
                .eq(Reservation::getRoomId, roomId)
                .eq(Reservation::getReservationDate, date)
                .list();

        // 3. 获取所有时间点并构建已预约时间段
        List<TimeRangeDto> reservedRanges = reservations.stream()
                .map(res -> {
                    TimePoint start = timePointService.getById(res.getStartTimeId());
                    TimePoint end = timePointService.getById(res.getEndTimeId());
                    if (start != null && end != null) {
                        TimeRangeDto dto = new TimeRangeDto();
                        dto.setStart(start.getPoint().toString());
                        dto.setEnd(end.getPoint().toString());
                        dto.setReservationNo(res.getReservationNo());
                        dto.setUserId(res.getUserId());  // 修改：使用 userId 而不是 studentId
                        return dto;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 4. 构建响应对象
        RoomReservationStatusResponse response = new RoomReservationStatusResponse();
        response.setRoomId(roomId);
        response.setRoomName(room.getName());
        response.setDate(date);
        response.setReservedTimeRanges(reservedRanges);

        return response;
    }


    /**
     * 核心方法：检查区间内是否有时间冲突
     * 逻辑：查询该教室+日期下，是否存在「与当前区间重叠」的已预约记录
     */
    private boolean hasTimeConflict(Integer roomId, LocalDate reservationDate, Integer startPointId, Integer endPointId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getRoomId, roomId)
                .eq(Reservation::getReservationDate, reservationDate)
                // 冲突条件：两个区间有重叠（参考区间重叠判断逻辑）
                // 现有预约的 start <= 当前区间的 end 且 现有预约的 end >= 当前区间的 start
                .le(Reservation::getStartTimeId, endPointId)
                .ge(Reservation::getEndTimeId, startPointId)
                // 待审核、已通过的预约都算占用（根据业务调整状态范围）
                .in(Reservation::getStatus, 0, 1);




        // 只要有冲突或不可用时间点，返回true
        return count(queryWrapper) > 0 ;
    }

    /**
     * 根据用户ID查询预约记录
     * @param userId
     * @param status
     * @return
     */
    @Override
    public List<ReservationVO> getReservationsByUserId(String userId, Integer status) {
        return reservationMapper.findByUserId(userId, status).stream()
                .map(reservation -> {
                    ReservationVO vo = new ReservationVO();
                    vo.setId(reservation.getId());
                    vo.setReservationNo(reservation.getReservationNo());
                    vo.setRoomId(reservation.getRoomId());
                    vo.setReservationDate(reservation.getReservationDate());
                    vo.setActivityName(reservation.getActivityName());
                    vo.setStatus(reservation.getStatus());
                    vo.setAttendees(reservation.getAttendees());
                    vo.setCreatedAt(reservation.getCreatedAt());

                    // 获取时间点信息
                    TimePointVO startTime = timePointService.getTimeSlotById(reservation.getStartTimeId());
                    TimePointVO endTime = timePointService.getTimeSlotById(reservation.getEndTimeId());
                    // 获得教室id
                    RoomResponse room = roomService.getRoomById(reservation.getRoomId());
                    // 获得预约审核信息
                    String statusDescription = reservationApprovalService.getStatusDescription(reservation.getStatus());
                    // 处理审核意见
                    String auditReason = reservationApprovalService.getLatestReasonByReservationId(reservation.getId());
                    vo.setAuditReason(auditReason);

                    vo.setRoomName(room.getName());
                    // 设置时间显示格式
                    vo.setStartTime(startTime.getPoint().toString()); // 转换为字符串格式
                    vo.setEndTime(endTime.getPoint().toString());
                    vo.setStatusDesc(statusDescription);

                    // 设置审核意见
                    vo.setAuditReason(auditReason);

                    return vo;
                })
                .collect(Collectors.toList());
    }


    /**
     * 取消预约
     * @param reservationNo
     * @param userId
     */
    @Override
    public void cancelReservation(String reservationNo, String userId) {

        // 1. 查询预约记录
        Reservation reservation = reservationMapper.selectByReservationNo(reservationNo)
                .orElseThrow(() -> new BusinessException("预约记录不存在"));

        // 2. 验证用户权限 - 修改：使用 userId 而不是 studentId
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权取消此预约");
        }

        // 3. 检查预约状态
        if (reservation.getStatus() != 0 && reservation.getStatus() != 1) {
            throw new BusinessException("当前状态不可取消");
        }

        // 4. 检查是否在可取消时间范围内
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancelDeadline = reservation.getReservationDate().atTime(
                getTimeById(reservation.getStartTimeId()).minusHours(1));

        if (now.isAfter(cancelDeadline)) {
            throw new BusinessException("已超过可取消时间（需在预约开始前1小时取消）");
        }

        // 5. 更新状态为已取消
        reservation.setStatus(3);
        reservationMapper.updateById(reservation);

        // —— 新增：判断当月内取消次数 ——
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        int cnt = reservationMapper.countByUserIdAndStatusAndCancelTimeAfter(
                userId, 3, oneMonthAgo);
        if (cnt >= 2) {
            // 记录惩罚：从第二次取消时刻起计算 3 个月
            LocalDateTime penaltyStart = LocalDateTime.now();
            LocalDateTime penaltyEnd   = penaltyStart.plusMonths(3);
            penaltyService.createPenalty(userId, penaltyStart, penaltyEnd);
        }
    }


    /**
     * 重新提交被退回的预约
     * @param reservationId 预约ID
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void resubmitReservation(Integer reservationId, String userId) {
//        try {
//            log.info("重新提交预约: reservationId={}, userId={}", reservationId, userId);
//
//            // 1. 查询预约记录
//            Reservation reservation = getById(reservationId);
//            if (reservation == null) {
//                throw new BusinessException("预约记录不存在");
//            }
//
//            // 2. 验证用户权限 - 修改：使用 userId 而不是 studentId
//            if (!reservation.getUserId().equals(userId)) {
//                throw new BusinessException("无权操作此预约");
//            }
//
//            // 3. 检查预约状态是否为被拒绝（2）
//            if (reservation.getStatus() != 2) {
//                throw new BusinessException("只有被拒绝的预约才能重新提交");
//            }
//
//            // 4. 检查时间冲突
//            TimePoint startPoint = timePointService.getById(reservation.getStartTimeId());
//            TimePoint endPoint = timePointService.getById(reservation.getEndTimeId());
//
//            if (startPoint == null || endPoint == null) {
//                throw new BusinessException("时间点数据异常");
//            }
//
//            if (hasTimeConflict(reservation.getRoomId(), reservation.getReservationDate(),
//                    startPoint.getPoint(), endPoint.getPoint())) {
//                throw new BusinessException("该时间段已被其他活动预定，无法重新提交");
//            }
//
//            // 5. 更新状态为待审核（0）
//            reservation.setStatus(0);
//            reservation.setUpdatedAt(LocalDateTime.now());
//
//            boolean updateSuccess = updateById(reservation);
//            if (!updateSuccess) {
//                throw new BusinessException("重新提交预约失败");
//            }
//
//            log.info("重新提交预约成功: reservationId={}", reservationId);
//
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("重新提交预约失败: reservationId={}, userId={}", reservationId, userId, e);
//            throw new BusinessException("重新提交预约失败: " + e.getMessage());
//        }
    }
    /**
     * 查询用户最近3次预约记录
     * @param userId
     * @return
     */
    @Override
    public List<ReservationVO> getLatestReservations(String userId) {

        // 1. 查询用户最近的3条预约记录，按创建时间降序排列 - 修改：使用 userId 而不是 studentId
        List<Reservation> reservations = lambdaQuery()
                .eq(Reservation::getUserId, userId)  // 修改：使用 userId 而不是 studentId
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

                    // 获取状态描述
                    String statusDescription = reservationApprovalService.getStatusDescription(reservation.getStatus());

                    // 设置额外信息
                    vo.setRoomName(room.getName());
                    vo.setStartTime(startTime.getPoint().toString());
                    vo.setEndTime(endTime.getPoint().toString());
                    vo.setStatusDesc(statusDescription);

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
}
