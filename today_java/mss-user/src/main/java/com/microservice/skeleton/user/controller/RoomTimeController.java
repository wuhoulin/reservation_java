package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.entity.RoomTime;
import com.microservice.skeleton.user.service.RoomTimeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/room-times")
@Api(tags = "教室时间段接口")
public class RoomTimeController {

    @Autowired
    private RoomTimeService roomTimeService;

    /**
     * 查询指定教室的所有时间段（含可用状态）
     */
    @GetMapping("/room/{roomId}")
    @ApiOperation("查询教室所有时间段")
    @ApiImplicitParam(name = "roomId", value = "教室ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<List<RoomTime>> getRoomTimePoints(@PathVariable Integer roomId) {
        List<RoomTime> timePoints = roomTimeService.getRoomAllTimePoints(roomId);
        return ApiResponse.success(timePoints);
    }
}
