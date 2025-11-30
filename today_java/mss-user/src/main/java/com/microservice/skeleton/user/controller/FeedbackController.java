package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.FeedbackRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.FeedbackResponse;
import com.microservice.skeleton.user.service.FeedbackService;
import com.microservice.skeleton.user.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@Api(tags = "意见反馈接口")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    @ApiOperation("提交反馈")
    public ApiResponse<FeedbackResponse> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        String userId = UserContext.getCurrentOpenid();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }

        FeedbackResponse response = feedbackService.submitFeedback(userId, request);
        return ApiResponse.success(response);
    }

    @GetMapping
    @ApiOperation("获取我的反馈列表")
    public ApiResponse<List<FeedbackResponse>> getMyFeedback() {
        String userId = UserContext.getCurrentOpenid();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }

        List<FeedbackResponse> feedbackList = feedbackService.getUserFeedback(userId);
        return ApiResponse.success(feedbackList);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取反馈详情")
    @ApiImplicitParam(name = "id", value = "反馈ID", required = true, dataType = "Long", paramType = "path")
    public ApiResponse<FeedbackResponse> getFeedbackDetail(@PathVariable Long id) {
        String userId = UserContext.getCurrentOpenid();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }

        FeedbackResponse response = feedbackService.getFeedbackDetail(id, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/admin/page")
    @ApiOperation("分页获取反馈列表（管理员）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页码", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "size", value = "每页数量", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "status", value = "状态筛选", dataType = "Integer", paramType = "query")
    })
    public ApiResponse<Page<FeedbackResponse>> getFeedbackPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        // 这里应该添加管理员权限验证
        Page<FeedbackResponse> page = feedbackService.getFeedbackPage(current, size, status);
        return ApiResponse.success(page);
    }

    @PostMapping("/admin/{id}/reply")
    @ApiOperation("回复反馈（管理员）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "反馈ID", required = true, dataType = "Long", paramType = "path"),
        @ApiImplicitParam(name = "replyContent", value = "回复内容", required = true, dataType = "String", paramType = "query")
    })
    public ApiResponse<Boolean> replyFeedback(
            @PathVariable Long id,
            @RequestParam String replyContent) {

        // 这里应该添加管理员权限验证，并获取管理员ID
        Long adminId = 1L; // 实际应该从登录信息中获取

        Boolean result = feedbackService.replyFeedback(id, replyContent, adminId);
        return ApiResponse.success(result);
    }

    @PatchMapping("/admin/{id}/status")
    @ApiOperation("更新反馈状态（管理员）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "反馈ID", required = true, dataType = "Long", paramType = "path"),
        @ApiImplicitParam(name = "status", value = "状态", required = true, dataType = "Integer", paramType = "query")
    })
    public ApiResponse<Boolean> updateFeedbackStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {

        // 这里应该添加管理员权限验证，并获取管理员ID
        Long adminId = 1L; // 实际应该从登录信息中获取

        Boolean result = feedbackService.updateFeedbackStatus(id, status, adminId);
        return ApiResponse.success(result);
    }
}
