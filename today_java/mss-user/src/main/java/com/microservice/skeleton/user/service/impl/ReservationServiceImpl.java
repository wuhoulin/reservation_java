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
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {

        // 1. 获取时间点数据
        List<TimePoint> timePoints = timePointService.listByIds(request.getTimePointIds());
        if (timePoints.size() != 2) {
            throw new BusinessException("时间点数据异常");
        }

        // 2. 自动排序时间点（确保 startTime < endTime）
        timePoints.sort(Comparator.comparing(TimePoint::getPoint));
        TimePoint startPoint = timePoints.get(0);
        TimePoint endPoint = timePoints.get(1);

        if (startPoint.getPoint().equals(endPoint.getPoint())) {
            throw new BusinessException("开始时间和结束时间不能相同");
        }

        // 3. 检查时间冲突（直接查询数据库）
        if (hasTimeConflict(request.getRoomId(), request.getReservationDate(),
                startPoint.getPoint(), endPoint.getPoint())) {
            throw new BusinessException("该时间段已被预约");
        }

        // 4. 保存预约记录
        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(request, reservation);
        reservation.setStartTimeId(startPoint.getId());
        reservation.setEndTimeId(endPoint.getId());
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
        reservation.setUserCn(request.getUserCn());
        reservation.setStudentId(request.getStudentId());
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());

        // 生成预约编号
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
                        dto.setUserId(res.getStudentId());
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
     * 检查时间段是否冲突（直接查询数据库）
     */
    private boolean hasTimeConflict(Integer roomId, LocalDate date, LocalTime start, LocalTime end) {
        // 查询该教室当天的所有预约记录
        List<Reservation> reservations = lambdaQuery()
                .eq(Reservation::getRoomId, roomId)
                .eq(Reservation::getReservationDate, date)
                .list();

        // 检查是否有时间冲突
        for (Reservation res : reservations) {
            TimePoint resStart = timePointService.getById(res.getStartTimeId());
            TimePoint resEnd = timePointService.getById(res.getEndTimeId());

            if (resStart != null && resEnd != null) {
                // 检查时间段是否重叠
                if (!(end.compareTo(resStart.getPoint()) <= 0 || start.compareTo(resEnd.getPoint()) >= 0)) {
                    return true; // 有冲突
                }
            }
        }
        return false; // 无冲突
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
                    // Map fields from entity to VO
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

                    // Additional fields that might need to be queried
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

        // 1. 查询预约记录 - 现在使用正确的方法名
        Reservation reservation = reservationMapper.selectByReservationNo(reservationNo)
                .orElseThrow(() -> new BusinessException("预约记录不存在"));

        // 2. 验证用户权限
        if (!reservation.getStudentId().equals(userId)) {
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
        reservationMapper.updateById(reservation); // 使用您现有的update方法

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
     * 查询用户最近3次预约记录
     * @param userId
     * @return
     */
    @Override
    public List<ReservationVO> getLatestReservations(String userId) {

        // 1. 查询用户最近的3条预约记录，按创建时间降序排列
        List<Reservation> reservations = lambdaQuery()
                .eq(Reservation::getStudentId, userId)
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
