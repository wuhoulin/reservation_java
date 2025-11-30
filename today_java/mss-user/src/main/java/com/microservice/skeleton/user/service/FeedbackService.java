package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.FeedbackRequest;
import com.microservice.skeleton.user.domain.Response.FeedbackResponse;
import java.util.List;

public interface FeedbackService {

    /**
     * 提交反馈
     */
    FeedbackResponse submitFeedback(String userId, FeedbackRequest request);

    /**
     * 获取用户的反馈列表
     */
    List<FeedbackResponse> getUserFeedback(String userId);

    /**
     * 获取反馈详情
     */
    FeedbackResponse getFeedbackDetail(Long id, String userId);

    /**
     * 分页获取反馈列表（管理员）
     */
    Page<FeedbackResponse> getFeedbackPage(Integer current, Integer size, Integer status);

    /**
     * 回复反馈（管理员）
     */
    Boolean replyFeedback(Long id, String replyContent, Long adminId);

    /**
     * 更新反馈状态（管理员）
     */
    Boolean updateFeedbackStatus(Long id, Integer status, Long adminId);
}
