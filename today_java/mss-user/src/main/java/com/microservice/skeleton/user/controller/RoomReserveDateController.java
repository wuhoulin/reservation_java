package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.entity.TimePoint;
import com.microservice.skeleton.user.service.RoomReserveDateService;
import com.microservice.skeleton.user.service.TimePointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roomReserveDate")
@Api(tags = "教室预约日期/时间点管理接口")
public class RoomReserveDateController {

    @Autowired
    private RoomReserveDateService roomReserveDateService;

    @Autowired
    private TimePointService timePointService;

    /**
     * 查询指定教室+日期下的可用时间点（未被占用的）
     * 前端请求：/api/roomReserveDate/available?roomId=1&reserveDate=2025-12-04
     */
    @GetMapping("/available")
    @ApiOperation("查询教室指定日期的可用时间点")
    public ApiResponse<List<TimePoint>> getAvailableTimePoints(
            @RequestParam Integer roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reserveDate) {

        // 1. 查询该教室+日期下所有已被占用的时间点ID
        List<Integer> occupiedTimePointIds = roomReserveDateService.getOccupiedTimePointIds(roomId, reserveDate);

        // 2. 查询系统所有时间点，过滤掉已占用的
        List<TimePoint> allTimePoints = timePointService.list();
        List<TimePoint> availableTimePoints = allTimePoints.stream()
                .filter(tp -> !occupiedTimePointIds.contains(tp.getId()))
                .sorted((a, b) -> a.getPoint().compareTo(b.getPoint())) // 按时间排序
                .collect(Collectors.toList());

        return ApiResponse.success(availableTimePoints);
    }
}
