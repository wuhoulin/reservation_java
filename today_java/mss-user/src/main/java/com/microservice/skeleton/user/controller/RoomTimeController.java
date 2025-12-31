package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.entity.RoomTime;
import com.microservice.skeleton.user.service.RoomTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/room-times")
@Tag(name = "教室时间段接口")
public class RoomTimeController {

    @Autowired
    private RoomTimeService roomTimeService;

    @GetMapping("/room/{roomId}")
    public ApiResponse<List<RoomTime>> getRoomTimePoints(@PathVariable Integer roomId) {
        List<RoomTime> timePoints = roomTimeService.getRoomAllTimePoints(roomId);
        return ApiResponse.success(timePoints);
    }
}
