package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.Request.FeedbackRequest;
import com.microservice.skeleton.user.domain.Response.FeedbackResponse;
import com.microservice.skeleton.user.domain.entity.Feedback;
import com.microservice.skeleton.user.mapper.FeedbackMapper;
import com.microservice.skeleton.user.service.FeedbackService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Override
    @Transactional
    public FeedbackResponse submitFeedback(String userId, FeedbackRequest request) {
        Feedback feedback = new Feedback();
        BeanUtils.copyProperties(request, feedback);
        feedback.setUserId(userId);
        feedback.setStatus(1); // 待处理

        this.save(feedback);

        return convertToResponse(feedback);
    }

    @Override
    public List<FeedbackResponse> getUserFeedback(String userId) {
        return this.baseMapper.selectFeedbackByUserId(userId);
    }

    @Override
    public FeedbackResponse getFeedbackDetail(Long id, String userId) {
        FeedbackResponse response = this.baseMapper.selectFeedbackDetail(id);
        if (response != null && !response.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此反馈");
        }
        return response;
    }

    @Override
    public Page<FeedbackResponse> getFeedbackPage(Integer current, Integer size, Integer status) {
        Page<FeedbackResponse> page = new Page<>(current, size);
        return this.baseMapper.selectFeedbackPage(page, status);
    }

    @Override
    @Transactional
    public Boolean replyFeedback(Long id, String replyContent, Long adminId) {
        Feedback feedback = this.getById(id);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }

        feedback.setReplyContent(replyContent);
        feedback.setRepliedAt(LocalDateTime.now());
        feedback.setRepliedBy(adminId);
        feedback.setStatus(3); // 标记为已处理

        return this.updateById(feedback);
    }

    @Override
    @Transactional
    public Boolean updateFeedbackStatus(Long id, Integer status, Long adminId) {
        Feedback feedback = this.getById(id);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }

        feedback.setStatus(status);
        if (status == 3) { // 如果是标记为已处理，记录回复信息
            feedback.setRepliedAt(LocalDateTime.now());
            feedback.setRepliedBy(adminId);
        }

        return this.updateById(feedback);
    }

    private FeedbackResponse convertToResponse(Feedback feedback) {
        FeedbackResponse response = new FeedbackResponse();
        BeanUtils.copyProperties(feedback, response);

        // 设置类型名称
        String[] typeNames = {"", "功能建议", "界面问题", "性能问题", "内容错误", "其他"};
        if (feedback.getType() != null && feedback.getType() > 0 && feedback.getType() < typeNames.length) {
            response.setTypeName(typeNames[feedback.getType()]);
        }

        // 设置状态名称
        String[] statusNames = {"", "待处理", "处理中", "已处理", "已关闭"};
        if (feedback.getStatus() != null && feedback.getStatus() > 0 && feedback.getStatus() < statusNames.length) {
            response.setStatusName(statusNames[feedback.getStatus()]);
        }

        return response;
    }
}
