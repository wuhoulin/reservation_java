package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Request.ReservationRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.ReservationResponse;
import com.microservice.skeleton.user.domain.Response.RoomReservationStatusResponse;
import com.microservice.skeleton.user.domain.vo.ReservationVO;
import com.microservice.skeleton.user.service.ReservationService;
import com.microservice.skeleton.user.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Api(tags = "预约管理接口")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    @ApiOperation("创建预约")
    public ApiResponse<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/room/{roomId}/status")
    @ApiOperation("查询教室预约状态")
    public ApiResponse<RoomReservationStatusResponse> getRoomReservationStatus(
            @PathVariable Integer roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // 如果未指定日期，默认查询当天
        if (date == null) {
            date = LocalDate.now();
        }

        RoomReservationStatusResponse response = reservationService.getRoomReservationStatus(roomId, date);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{reservationNo}/cancel")
    @ApiOperation("取消预约")
    public ApiResponse<Void> cancelReservation(
            @PathVariable String reservationNo,
            @RequestParam String userId) {
        reservationService.cancelReservation(reservationNo, userId);
        return ApiResponse.success();
    }

    @PatchMapping("/{reservationId}/resubmit")
    @ApiOperation("重新提交被退回的预约")
    public ApiResponse<Void> resubmitReservation(
            @PathVariable Integer reservationId,
            @RequestParam String userId) {

        String openid = UserContext.getCurrentOpenid();
        if (openid == null) {
            return ApiResponse.error(401, "用户未登录");
        }

        if (!openid.equals(userId)) {
            return ApiResponse.error(403, "无权操作");
        }

        reservationService.resubmitReservation(reservationId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/user/reservations")
    @ApiOperation("获取当前用户的预约记录")
    public ApiResponse<List<ReservationVO>> getCurrentUserReservations(
            @RequestParam(required = false) Integer status) {

        String openid = UserContext.getCurrentOpenid();

        if (openid == null) {
            return ApiResponse.error(401, "用户未登录");
        }

        List<ReservationVO> reservations = reservationService.getReservationsByUserId(openid, status);
        return ApiResponse.success(reservations);
    }

    @GetMapping("/latest")
    @ApiOperation("查询最新的3条预约记录")
    public ApiResponse<List<ReservationVO>> getLatestReservations() {
        String openid = UserContext.getCurrentOpenid();
        if (openid == null) {
            return ApiResponse.error(401, "用户未登录");
        }

        List<ReservationVO> latestReservations = reservationService.getLatestReservations(openid);
        return ApiResponse.success(latestReservations);
    }
}
