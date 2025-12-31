package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.Response.ActivitySignupResponse;
import com.microservice.skeleton.user.domain.entity.ActivitySignup;

public interface ActivitySignupService extends IService<ActivitySignup> {

    /**
     * 报名活动
     */
    void joinActivity(Long activityId, String userId);

    /**
     * 取消报名
     */
    void cancelSignup(Long activityId, String userId);

    /**
     * 检查是否已报名
     */
    boolean checkIsJoined(Long activityId, String userId);

    /**
     * 获取我的报名列表（分页）
     */
    Page<ActivitySignupResponse> getMySignupPage(Page<ActivitySignupResponse> page, String userId);
}
