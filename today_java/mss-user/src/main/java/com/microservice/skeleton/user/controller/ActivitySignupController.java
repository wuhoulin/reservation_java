package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Response.ActivitySignupResponse;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.PageResult;
import com.microservice.skeleton.user.service.ActivitySignupService;
import com.microservice.skeleton.user.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity-signups")
@Tag(name = "活动报名管理")
@CrossOrigin
public class ActivitySignupController {

    @Autowired
    private ActivitySignupService signupService;

    @PostMapping("/join")
    @Operation(summary = "报名参加活动")
    @Parameter(name = "activityId", description = "活动ID", required = true)
    public ApiResponse<Void> join(@RequestParam Long activityId) {
        String openid = UserContext.getCurrentOpenid();
        if (openid == null || openid.trim().isEmpty()) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        signupService.joinActivity(activityId, openid);
        return ApiResponse.success();
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消报名")
    @Parameter(name = "activityId", description = "活动ID", required = true)
    public ApiResponse<Void> cancel(@RequestParam Long activityId) {
        String openid = UserContext.getCurrentOpenid();
        if (openid == null || openid.trim().isEmpty()) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        signupService.cancelSignup(activityId, openid);
        return ApiResponse.success();
    }

    @GetMapping("/is-joined")
    @Operation(summary = "检查是否已报名")
    @Parameter(name = "activityId", description = "活动ID", required = true)
    public ApiResponse<Boolean> checkJoined(@RequestParam Long activityId) {
        String openid = UserContext.getCurrentOpenid();
        if (openid == null || openid.trim().isEmpty()) {
            return ApiResponse.error(401, "用户未登录");
        }

        boolean result = signupService.checkIsJoined(activityId, openid);
        return ApiResponse.success(result);
    }

    @GetMapping("/my-page")
    public ApiResponse<PageResult<ActivitySignupResponse>> getMySignups(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        String openid = UserContext.getCurrentOpenid();
        if (openid == null || openid.trim().isEmpty()) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Page<ActivitySignupResponse> page = new Page<>(current, size);
        Page<ActivitySignupResponse> result = signupService.getMySignupPage(page, openid);

        PageResult<ActivitySignupResponse> pageResult = new PageResult<>();
        pageResult.setRecords(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());

        return ApiResponse.success(pageResult);
    }
}
