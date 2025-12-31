package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.Request.ActivityRequest;
import com.microservice.skeleton.user.domain.Response.ActivityResponse;
import com.microservice.skeleton.user.domain.entity.Activity;

public interface ActivityService extends IService<Activity> {
    ActivityResponse createActivity(ActivityRequest request);
    ActivityResponse updateActivity(Long id, ActivityRequest request);
    ActivityResponse getActivityById(Long id);
    Page<ActivityResponse> getActivityPage(Page<Activity> page, String keyword, String status);
}
