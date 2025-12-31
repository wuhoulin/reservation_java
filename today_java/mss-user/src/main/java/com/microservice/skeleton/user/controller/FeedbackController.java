package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.FeedbackRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.FeedbackResponse;
import com.microservice.skeleton.user.service.FeedbackService;
import com.microservice.skeleton.user.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@Tag(name = "意见反馈接口")
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "提交反馈")
    public ApiResponse<FeedbackResponse> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        String userId = UserContext.getCurrentOpenid();
        if (userId == null || userId.trim().isEmpty()) {
            log.error("提交反馈失败：用户ID为空，用户未登录");
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        FeedbackResponse response = feedbackService.submitFeedback(userId, request);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<FeedbackResponse>> getMyFeedback() {
        String userId = UserContext.getCurrentOpenid();
        if (userId == null || userId.trim().isEmpty()) {
            log.error("获取我的反馈列表失败：用户ID为空，用户未登录");
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        List<FeedbackResponse> feedbackList = feedbackService.getUserFeedback(userId);
        return ApiResponse.success(feedbackList);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取反馈详情")
    @Parameter(name = "id", description = "反馈ID", required = true)
    public ApiResponse<FeedbackResponse> getFeedbackDetail(@PathVariable Long id) {
        String userId = UserContext.getCurrentOpenid();
        if (userId == null || userId.trim().isEmpty()) {
            log.error("获取反馈详情失败：用户ID为空，用户未登录，反馈ID：{}", id);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        FeedbackResponse response = feedbackService.getFeedbackDetail(id, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/admin/page")
    @Operation(summary = "分页获取反馈列表（管理员）")
    @Parameters({
            @Parameter(name = "current", description = "当前页码", required = true),
            @Parameter(name = "size", description = "每页数量", required = true),
            @Parameter(name = "status", description = "状态筛选")
    })
    public ApiResponse<Page<FeedbackResponse>> getFeedbackPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        String adminOpenId = UserContext.getCurrentOpenid();
        if (adminOpenId == null || adminOpenId.trim().isEmpty()) {
            log.error("管理员分页获取反馈列表失败：管理员未登录");
            return ApiResponse.error(401, "管理员未登录，请重新登录后再试");
        }

        Page<FeedbackResponse> page = feedbackService.getFeedbackPage(current, size, status);
        return ApiResponse.success(page);
    }

    @PostMapping("/admin/{id}/reply")
    public ApiResponse<Boolean> replyFeedback(
            @PathVariable Long id,
            @RequestParam String replyContent) {

        String adminOpenId = UserContext.getCurrentOpenid();
        if (adminOpenId == null || adminOpenId.trim().isEmpty()) {
            log.error("管理员回复反馈失败：管理员未登录，反馈ID：{}", id);
            return ApiResponse.error(401, "管理员未登录，请重新登录后再试");
        }

        Long adminId = 1L;

        Boolean result = feedbackService.replyFeedback(id, replyContent, adminId);
        return ApiResponse.success(result);
    }

    @PatchMapping("/admin/{id}/status")
    public ApiResponse<Boolean> updateFeedbackStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {

        String adminOpenId = UserContext.getCurrentOpenid();
        if (adminOpenId == null || adminOpenId.trim().isEmpty()) {
            log.error("管理员更新反馈状态失败：管理员未登录，反馈ID：{}", id);
            return ApiResponse.error(401, "管理员未登录，请重新登录后再试");
        }

        Long adminId = 1L;

        Boolean result = feedbackService.updateFeedbackStatus(id, status, adminId);
        return ApiResponse.success(result);
    }
}
