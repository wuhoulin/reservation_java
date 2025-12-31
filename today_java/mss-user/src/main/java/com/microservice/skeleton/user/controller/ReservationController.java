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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "预约管理接口", description = "处理教室预约、取消、签到及状态查询")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    @Operation(summary = "创建预约", description = "提交新的教室预约申请")
    public ApiResponse<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/room/{roomId}/status")
    @Operation(summary = "查询教室预约状态", description = "获取指定教室在某一天的预约占用情况")
    public ApiResponse<RoomReservationStatusResponse> getRoomReservationStatus(
            @Parameter(description = "教室ID") @PathVariable Integer roomId,
            @Parameter(description = "查询日期(yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        RoomReservationStatusResponse response = reservationService.getRoomReservationStatus(roomId, date);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{reservationNo}/cancel")
    @Operation(summary = "取消预约", description = "根据预约编号取消尚未进行的预约")
    public ApiResponse<Void> cancelReservation(@Parameter(description = "预约编号") @PathVariable String reservationNo) {
        String openid = UserContext.getCurrentOpenid();

        if (openid == null || openid.trim().isEmpty()) {
            log.error("取消预约失败：用户未登录，预约编号：{}", reservationNo);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        reservationService.cancelReservation(reservationNo, openid);
        return ApiResponse.success();
    }

    @PatchMapping("/{reservationId}/resubmit")
    @Operation(summary = "重新提交预约", description = "修改并重新提交被退回的预约申请")
    public ApiResponse<Void> resubmitReservation(
            @Parameter(description = "原预约ID") @PathVariable Integer reservationId,
            @Parameter(description = "用户ID") @RequestParam String userId) {

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
    @Operation(summary = "获取当前用户的预约记录", description = "查询当前登录用户的历史及当前预约列表")
    @Parameters({
            @Parameter(name = "status", description = "预约状态过滤(0-待审核, 1-已通过, 等或传'all')"),
            @Parameter(name = "roomId", description = "教室ID过滤")
    })
    public ApiResponse<List<ReservationVO>> getCurrentUserReservations(
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "roomId", required = false) Integer roomId) {

        String openid = UserContext.getCurrentOpenid();

        if (openid == null || openid.trim().isEmpty()) {
            log.error("获取用户预约记录失败：用户未登录，状态参数：{}", statusStr);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Integer status = null;
        if (statusStr != null && !statusStr.trim().isEmpty() && !"all".equalsIgnoreCase(statusStr)) {
            try {
                status = Integer.parseInt(statusStr.trim());
            } catch (NumberFormatException e) {
                log.warn("获取用户预约记录：状态参数格式错误，statusStr={}，用户={}", statusStr, openid);
            }
        }

        List<ReservationVO> reservations = reservationService.getReservationsByUserId(openid, status, roomId);
        return ApiResponse.success(reservations);
    }

    @GetMapping("/latest")
    @Operation(summary = "查询最新的3条预约记录", description = "首页快捷查看最近的预约动态")
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
    @Operation(summary = "获取预约详情", description = "根据预约ID获取详细的单条预约信息")
    public ApiResponse<ReservationVO> getReservationDetail(@Parameter(description = "预约ID") @PathVariable Integer id) {
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

    @PostMapping("/check-in")
    @Operation(summary = "用户现场签到", description = "用户到达现场后通过经纬度或扫码进行签到")
    public ApiResponse<String> checkIn(@RequestBody CheckInRequest request) {
        String userId = UserContext.getCurrentOpenid();
        reservationService.performCheckIn(userId, request);
        return ApiResponse.success("签到成功！");
    }

    @GetMapping("/current-check-in")
    @Operation(summary = "获取当前需要签到的预约任务", description = "获取当前时间点或临近时间点需要签到的预约记录")
    public ApiResponse<ReservationVO> getCurrentCheckInTask() {
        String openid = UserContext.getCurrentOpenid();
        ReservationVO task = reservationService.findCurrentCheckInTask(openid);
        if (task == null) {
            return ApiResponse.success(null);
        }
        return ApiResponse.success(task);
    }

    @GetMapping("/check-in-state")
    @Operation(summary = "获取签到页面状态", description = "获取用户当前的整体签到权限和状态视图")
    public ApiResponse<CheckInStateVO> getCheckInState() {
        String openid = UserContext.getCurrentOpenid();
        CheckInStateVO state = reservationService.getCheckInState(openid);
        return ApiResponse.success(state);
    }

    @GetMapping("/room/{roomId}/pending-counts")
    @Operation(summary = "查询待审核人数", description = "查询某天某教室各时间段内处于待审核状态的申请数量")
    public ApiResponse<Map<Integer, Integer>> getPendingReservationCounts(
            @Parameter(description = "教室ID") @PathVariable Integer roomId,
            @Parameter(description = "查询日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<Integer, Integer> counts = reservationService.getPendingCounts(roomId, date);
        return ApiResponse.success(counts);
    }
}
