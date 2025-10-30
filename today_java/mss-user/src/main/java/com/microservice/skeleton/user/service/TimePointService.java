package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.Request.TimePointRequest;
import com.microservice.skeleton.user.domain.Response.TimePointResponse;
import com.microservice.skeleton.user.domain.entity.TimePoint;
import com.microservice.skeleton.user.domain.vo.TimePointVO;

import java.time.LocalDate;
import java.util.List;

public interface TimePointService extends IService<TimePoint> {

    TimePointResponse createTimePoint(TimePointRequest request);

    TimePointResponse updateTimePoint(Integer id, TimePointRequest request);

    void deleteTimePoint(Integer id);

    TimePointResponse getTimePointById(Integer id);

    List<TimePointResponse> getAllTimePoints();

    List<TimePointResponse> getAvailableTimePoints();

    List<TimePointVO> getAvailableTimePointsForRoom(Integer roomId, LocalDate date);

    TimePointVO getTimeSlotById(Integer timeId);
}
