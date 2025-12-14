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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    @ApiOperation("创建预约")
    public ApiResponse<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        // 注：创建预约的用户登录校验已在 Service 层完成，此处保持原有逻辑
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

        // 关键修改：移除测试用默认值，改为返回未登录错误
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
            @RequestParam(required = false) String statusStr) { // 先接收为String类型做兼容

        String openid = UserContext.getCurrentOpenid();

        // 1. 校验用户登录状态
        if (openid == null || openid.trim().isEmpty()) {
//            openid="oAnc9vgK495dktuO_F43WR3fkrzg";
            log.error("获取用户预约记录失败：用户未登录，状态参数：{}", statusStr);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        // 2. 处理status参数，容错转换为Integer
        Integer status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                // 过滤掉NaN、空字符串等无效值
                status = Integer.parseInt(statusStr.trim());
                // 可选：校验status的合法范围（根据业务定义，比如0-4）
                if (status < 0 || status > 4) {
                    log.warn("获取用户预约记录：状态值超出合法范围，status={}，用户={}", status, openid);
                    status = null; // 非法值置为null，查询全部
                }
            } catch (NumberFormatException e) {
                log.warn("获取用户预约记录：状态参数格式错误，statusStr={}，用户={}", statusStr, openid, e);
                status = null; // 转换失败时置为null，查询全部
            }
        }

        // 3. 调用服务查询
        List<ReservationVO> reservations = reservationService.getReservationsByUserId(openid, status);
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

        // 调试用：如果没有登录，给一个默认账号（生产环境请务必删除或改为报错）
        if (openid == null || openid.trim().isEmpty()) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        // 1. 查询数据
        // 注意：这里变量名改成了单数 reservation，对应下面的判断
        ReservationVO reservation = reservationService.getReservationDetail(id);

        // 2. 判空校验
        if (reservation == null) {
            log.error("预约详情不存在，预约ID：{}", id);
            return ApiResponse.error(404, "预约不存在");
        }



        return ApiResponse.success(reservation);
    }
}
