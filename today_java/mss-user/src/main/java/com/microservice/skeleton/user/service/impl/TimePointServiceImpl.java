package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.Request.TimePointRequest;
import com.microservice.skeleton.user.domain.Response.TimePointResponse;
import com.microservice.skeleton.user.domain.entity.Reservation;
import com.microservice.skeleton.user.domain.entity.TimePoint;
import com.microservice.skeleton.user.domain.vo.TimePointVO;
import com.microservice.skeleton.user.mapper.ReservationMapper;
import com.microservice.skeleton.user.mapper.TimePointMapper;
import com.microservice.skeleton.user.service.RoomService;
import com.microservice.skeleton.user.service.TimePointService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TimePointServiceImpl extends ServiceImpl<TimePointMapper, TimePoint> implements TimePointService {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    @Transactional
    public TimePointResponse createTimePoint(TimePointRequest request) {
        // 检查时间点是否已存在
        LambdaQueryWrapper<TimePoint> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TimePoint::getPoint, request.getPoint());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("该时间点已存在");
        }

        // 创建新时间点
        TimePoint timePoint = new TimePoint();
        BeanUtils.copyProperties(request, timePoint);
        timePoint.setStatus(1); // 默认设置为可用

        // 保存时间点
        this.save(timePoint);

        // 转换为响应对象
        TimePointResponse response = new TimePointResponse();
        BeanUtils.copyProperties(timePoint, response);

        return response;
    }

    @Override
    @Transactional
    public TimePointResponse updateTimePoint(Integer id, TimePointRequest request) {
        // 检查时间点是否存在
        TimePoint timePoint = this.getById(id);
        if (timePoint == null) {
            throw new BusinessException("时间点不存在");
        }

        // 检查时间点是否已被其他记录使用
        LambdaQueryWrapper<TimePoint> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TimePoint::getPoint, request.getPoint())
                .ne(TimePoint::getId, id);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("该时间点已存在");
        }

        // 更新时间点信息
        timePoint.setPoint(request.getPoint());
        timePoint.setStatus(request.getStatus());

        // 保存时间点
        this.updateById(timePoint);

        // 转换为响应对象
        TimePointResponse response = new TimePointResponse();
        BeanUtils.copyProperties(timePoint, response);

        return response;
    }

    @Override
    @Transactional
    public void deleteTimePoint(Integer id) {
        // 检查时间点是否存在
        if (!this.removeById(id)) {
            throw new BusinessException("时间点不存在");
        }
    }

    @Override
    public TimePointResponse getTimePointById(Integer id) {
        // 检查时间点是否存在
        TimePoint timePoint = this.getById(id);
        if (timePoint == null) {
            throw new BusinessException("时间点不存在");
        }

        // 转换为响应对象
        TimePointResponse response = new TimePointResponse();
        BeanUtils.copyProperties(timePoint, response);

        return response;
    }

    @Override
    public List<TimePointResponse> getAllTimePoints() {
        // 获取所有时间点
        List<TimePoint> timePoints = this.list();

        // 转换为响应对象列表
        return timePoints.stream()
                .map(timePoint -> {
                    TimePointResponse response = new TimePointResponse();
                    BeanUtils.copyProperties(timePoint, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TimePointResponse> getAvailableTimePoints() {
        // 获取可用时间点（status=1）
        LambdaQueryWrapper<TimePoint> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TimePoint::getStatus, 1)
                .orderByAsc(TimePoint::getPoint);
        List<TimePoint> timePoints = this.list(queryWrapper);

        // 转换为响应对象列表
        return timePoints.stream()
                .map(timePoint -> {
                    TimePointResponse response = new TimePointResponse();
                    BeanUtils.copyProperties(timePoint, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TimePointVO> getAvailableTimePointsForRoom(Integer roomId, LocalDate date) {
        // 检查教室是否存在
        if (roomService.getById(roomId) == null) {
            throw new BusinessException("教室不存在");
        }

        // 获取该教室在指定日期的所有预约记录
        List<Reservation> reservations = reservationMapper.findByRoomIdAndDate(roomId, date);

        // 获取所有时间点
        List<TimePoint> allTimePoints = list(
                new QueryWrapper<TimePoint>()
                        .eq("status", 1)
                        .orderByAsc("point")
        );

        // 找出已被预约的时间点ID
        Set<Integer> reservedTimePointIds = reservations.stream()
                .flatMap(r -> Stream.of(r.getStartTimeId(), r.getEndTimeId()))
                .collect(Collectors.toSet());

        // 过滤出可用时间点
        return allTimePoints.stream()
                .filter(tp -> !reservedTimePointIds.contains(tp.getId()))
                .map(tp -> {
                    TimePointVO vo = new TimePointVO();
                    BeanUtils.copyProperties(tp, vo);
                    vo.setAvailable(true); // 这些是未被预约的时间点
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public TimePointVO getTimeSlotById(Integer timeId) {
        TimePoint timePoint = this.getById(timeId);
        if (timePoint == null) {
            throw new BusinessException("时间点不存在");
        }

        TimePointVO vo = new TimePointVO();
        BeanUtils.copyProperties(timePoint, vo);
        return vo;
    }
}
