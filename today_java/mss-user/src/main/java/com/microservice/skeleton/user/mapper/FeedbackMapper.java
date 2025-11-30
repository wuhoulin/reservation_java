package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.entity.Feedback;
import com.microservice.skeleton.user.domain.Response.FeedbackResponse;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface FeedbackMapper extends BaseMapper<Feedback> {

    /**
     * 获取用户的反馈列表
     */
    List<FeedbackResponse> selectFeedbackByUserId(@Param("userId") String userId);

    /**
     * 分页获取反馈列表（管理员用）
     */
    Page<FeedbackResponse> selectFeedbackPage(Page<FeedbackResponse> page, @Param("status") Integer status);

    /**
     * 获取反馈详情
     */
    FeedbackResponse selectFeedbackDetail(@Param("id") Long id);
}
