package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Request.CheckInRequest;
import com.microservice.skeleton.user.domain.Request.ReservationRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.ReservationResponse;
import com.microservice.skeleton.user.domain.Response.RoomReservationStatusResponse;
import com.microservice.skeleton.user.domain.vo.CheckInStateVO;
import com.microservice.skeleton.user.domain.vo.ReservationVO;
import com.microservice.skeleton.user.service.ReservationService;
import com.microservice.skeleton.user.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@Api(tags = "预约管理接口")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

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
    public ApiResponse<Void> cancelReservation(@PathVariable String reservationNo) {
        String openid = UserContext.getCurrentOpenid();

        if (openid == null || openid.trim().isEmpty()) {
            log.error("取消预约失败：用户未登录，预约编号：{}", reservationNo);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        reservationService.cancelReservation(reservationNo, openid);
        return ApiResponse.success();
    }

    @PatchMapping("/{reservationId}/resubmit")
    @ApiOperation("重新提交被退回的预约")
    public ApiResponse<Void> resubmitReservation(
            @PathVariable Integer reservationId,
            @RequestParam String userId) {

        String openid = UserContext.getCurrentOpenid();
        if (openid == null || openid.trim().isEmpty()) {
            log.error("重新提交预约失败：用户未登录，预约ID：{}", reservationId);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        if (!openid.equals(userId)) {
            log.error("重新提交预约权限不足：当前用户{}，操作用户{}，预约ID：{}", openid, userId, reservationId);
            return ApiResponse.error(403, "无权操作此预约");
        }

        reservationService.resubmitReservation(reservationId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/user/reservations")
    @ApiOperation("获取当前用户的预约记录")
    public ApiResponse<List<ReservationVO>> getCurrentUserReservations(
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "roomId", required = false) Integer roomId) {

        String openid = UserContext.getCurrentOpenid();

        // 校验用户登录状态
        if (openid == null || openid.trim().isEmpty()) {
            log.error("获取用户预约记录失败：用户未登录，状态参数：{}", statusStr);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        // 处理status参数
        Integer status = null;
        if (statusStr != null && !statusStr.trim().isEmpty() && !"all".equalsIgnoreCase(statusStr)) {
            try {
                status = Integer.parseInt(statusStr.trim());
            } catch (NumberFormatException e) {
                log.warn("获取用户预约记录：状态参数格式错误，statusStr={}，用户={}", statusStr, openid, e);
                status = null;
            }
        }

        // 调用服务查询，传入roomId
        List<ReservationVO> reservations = reservationService.getReservationsByUserId(openid, status, roomId);
        return ApiResponse.success(reservations);
    }

    @GetMapping("/latest")
    @ApiOperation("查询最新的3条预约记录")
    public ApiResponse<List<ReservationVO>> getLatestReservations() {
        String openid = UserContext.getCurrentOpenid();
        if (openid == null || openid.trim().isEmpty()) {
            log.error("查询最新预约记录失败：用户未登录");
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        List<ReservationVO> latestReservations = reservationService.getLatestReservations(openid);
        return ApiResponse.success(latestReservations);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取预约详情")
    public ApiResponse<ReservationVO> getReservationDetail(@PathVariable Integer id) {
        String openid = UserContext.getCurrentOpenid();

        if (openid == null || openid.trim().isEmpty()) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }
        ReservationVO reservation = reservationService.getReservationDetail(id);

        if (reservation == null) {
            log.error("预约详情不存在，预约ID：{}", id);
            return ApiResponse.error(404, "预约不存在");
        }

        return ApiResponse.success(reservation);
    }

    /**
     * 用户现场签到接口
     */
    @PostMapping("/check-in")
    public ApiResponse checkIn(@RequestBody CheckInRequest request) {
        String userId= UserContext.getCurrentOpenid();
        reservationService.performCheckIn(userId, request);
        return ApiResponse.success("签到成功！");
    }

    @GetMapping("/current-check-in")
    @ApiOperation("获取当前需要签到的预约任务")
    public ApiResponse<ReservationVO> getCurrentCheckInTask() {
        String openid = UserContext.getCurrentOpenid();
        ReservationVO task = reservationService.findCurrentCheckInTask(openid);
        if (task == null) {
            return ApiResponse.success(null); // 没有任务
        }
        return ApiResponse.success(task);
    }

    @GetMapping("/check-in-state")
    @ApiOperation("获取签到页面状态")
    public ApiResponse<CheckInStateVO> getCheckInState() {
        String openid = UserContext.getCurrentOpenid();
        CheckInStateVO state = reservationService.getCheckInState(openid);
        return ApiResponse.success(state);
    }


    @GetMapping("/room/{roomId}/pending-counts")
    @ApiOperation("查询某天教室各时间段的待审核人数")
    public ApiResponse<Map<Integer, Integer>> getPendingReservationCounts(
            @PathVariable Integer roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<Integer, Integer> counts = reservationService.getPendingCounts(roomId, date);
        return ApiResponse.success(counts);
    }
}
