package com.microservice.skeleton.user.controller;


import com.microservice.skeleton.user.domain.Request.TimePointRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.TimePointResponse;
import com.microservice.skeleton.user.domain.vo.TimePointVO;
import com.microservice.skeleton.user.service.TimePointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/time-points")
@Api(tags = "时间点管理接口")
public class TimePointController {

    @Autowired
    private TimePointService timePointService;

    @PostMapping
    @ApiOperation("创建时间点")
    public ApiResponse<TimePointResponse> createTimePoint(@Valid @RequestBody TimePointRequest request) {
        TimePointResponse response = timePointService.createTimePoint(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新时间点")
    @ApiImplicitParam(name = "id", value = "时间点ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<TimePointResponse> updateTimePoint(@PathVariable Integer id, @Valid @RequestBody TimePointRequest request) {
        TimePointResponse response = timePointService.updateTimePoint(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除时间点")
    @ApiImplicitParam(name = "id", value = "时间点ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<Void> deleteTimePoint(@PathVariable Integer id) {
        timePointService.deleteTimePoint(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("获取时间点详情")
    @ApiImplicitParam(name = "id", value = "时间点ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<TimePointResponse> getTimePoint(@PathVariable Integer id) {
        TimePointResponse response = timePointService.getTimePointById(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    @ApiOperation("获取所有时间点")
    public ApiResponse<List<TimePointResponse>> getAllTimePoints() {
        List<TimePointResponse> responses = timePointService.getAllTimePoints();
        return ApiResponse.success(responses);
    }

    @GetMapping("/available")
    @ApiOperation("获取可用时间点")
    public ApiResponse<List<TimePointResponse>> getAvailableTimePoints() {
        List<TimePointResponse> responses = timePointService.getAvailableTimePoints();
        return ApiResponse.success(responses);
    }

    @GetMapping("/available-for-room")
    @ApiOperation("获取教室在指定日期的可用时间点")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roomId", value = "教室ID", required = true, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "date", value = "日期", required = true, dataType = "String", paramType = "query")
    })
    public ApiResponse<List<TimePointVO>> getAvailableTimePointsForRoom(@RequestParam Integer roomId,
                                                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<TimePointVO> vos = timePointService.getAvailableTimePointsForRoom(roomId, date);
        return ApiResponse.success(vos);
    }
}
