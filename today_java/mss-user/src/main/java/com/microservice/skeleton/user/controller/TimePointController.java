package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Request.TimePointRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.TimePointResponse;
import com.microservice.skeleton.user.domain.vo.TimePointVO;
import com.microservice.skeleton.user.service.TimePointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/time-points")
@Tag(name = "时间点管理接口", description = "用于管理和查询教室预约的时间段")
public class TimePointController {

    @Autowired
    private TimePointService timePointService;

    @PostMapping
    @Operation(summary = "创建时间点", description = "新增一个可供预约的时间点（如 08:30）")
    public ApiResponse<TimePointResponse> createTimePoint(@Valid @RequestBody TimePointRequest request) {
        TimePointResponse response = timePointService.createTimePoint(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新时间点", description = "根据ID修改时间点信息")
    public ApiResponse<TimePointResponse> updateTimePoint(
            @Parameter(description = "时间点ID", required = true) @PathVariable Integer id,
            @Valid @RequestBody TimePointRequest request) {
        TimePointResponse response = timePointService.updateTimePoint(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除时间点", description = "根据ID删除时间点")
    public ApiResponse<Void> deleteTimePoint(
            @Parameter(description = "时间点ID", required = true) @PathVariable Integer id) {
        timePointService.deleteTimePoint(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取时间点详情", description = "根据ID查询具体的时间点信息")
    public ApiResponse<TimePointResponse> getTimePoint(
            @Parameter(description = "时间点ID", required = true) @PathVariable Integer id) {
        TimePointResponse response = timePointService.getTimePointById(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "获取所有时间点", description = "查询系统中定义的所有时间点列表")
    public ApiResponse<List<TimePointResponse>> getAllTimePoints() {
        List<TimePointResponse> responses = timePointService.getAllTimePoints();
        return ApiResponse.success(responses);
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用时间点", description = "查询当前状态为启用的时间点")
    public ApiResponse<List<TimePointResponse>> getAvailableTimePoints() {
        List<TimePointResponse> responses = timePointService.getAvailableTimePoints();
        return ApiResponse.success(responses);
    }

    @GetMapping("/available-for-room")
    @Operation(summary = "获取教室在指定日期的可用时间点", description = "排除掉指定教室在该日期已被占用的时间段")
    @Parameters({
            @Parameter(name = "roomId", description = "教室ID", required = true),
            @Parameter(name = "date", description = "日期(yyyy-MM-dd)", required = true)
    })
    public ApiResponse<List<TimePointVO>> getAvailableTimePointsForRoom(
            @RequestParam Integer roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        // 修正拼写错误：getAvailableTimePointsForRo -> getAvailableTimePointsForRoom
        List<TimePointVO> vos = timePointService.getAvailableTimePointsForRoom(roomId, date);
        return ApiResponse.success(vos);
    }
}
