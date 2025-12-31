package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.Request.ActivityRequest;
import com.microservice.skeleton.user.domain.Response.ActivityResponse;
import com.microservice.skeleton.user.domain.entity.Activity;
import com.microservice.skeleton.user.domain.entity.ActivitySignup;
import com.microservice.skeleton.user.mapper.ActivityMapper;
import com.microservice.skeleton.user.mapper.ActivitySignupMapper;
import com.microservice.skeleton.user.service.ActivityService;
import com.microservice.skeleton.user.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private ActivitySignupMapper activitySignupMapper;

    @Override
    @Transactional
    public ActivityResponse createActivity(ActivityRequest request) {
        Activity activity = new Activity();
        BeanUtils.copyProperties(request, activity);
        // 初始化默认值
        activity.setCurrentPeople(0);
        activity.setViewCount(0L);
        if (StringUtils.isEmpty(activity.getStatus())) {
            activity.setStatus("0"); // 默认报名中
        }
        this.save(activity);
        return convertToResponse(activity);
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(Long id, ActivityRequest request) {
        Activity activity = this.getById(id);
        if (activity != null) {
            BeanUtils.copyProperties(request, activity);
            activity.setActivityId(id);
            this.updateById(activity);
            return convertToResponse(activity);
        }
        return null;
    }

    @Override
    public ActivityResponse getActivityById(Long id) {
        Activity activity = this.getById(id);
        if (activity != null) {
            baseMapper.incrementViewCount(id);
            activity.setViewCount(activity.getViewCount() + 1);
            return convertToResponse(activity);
        }
        return null;
    }

    @Override
    public Page<ActivityResponse> getActivityPage(Page<Activity> page, String keyword, String status) {
        // 1. 原有的活动查询逻辑
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Activity::getTitle, keyword)
                    .or().like(Activity::getSummary, keyword));
        }
        if (StringUtils.hasText(status) && !"all".equals(status)) {
            wrapper.eq(Activity::getStatus, status);
        }
        wrapper.orderByDesc(Activity::getIsRecommend)
                .orderByDesc(Activity::getStartTime);

        Page<Activity> result = this.page(page, wrapper);


        List<ActivityResponse> list = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());


        String currentUserOpenid = UserContext.getCurrentOpenid();

        if (StringUtils.hasText(currentUserOpenid) && !list.isEmpty()) {
            // (1) 拿到当前页所有活动的ID列表
            List<Long> activityIds = list.stream()
                    .map(ActivityResponse::getActivityId)
                    .collect(Collectors.toList());


            LambdaQueryWrapper<ActivitySignup> signupWrapper = new LambdaQueryWrapper<>();
            signupWrapper.in(ActivitySignup::getActivityId, activityIds)
                    .eq(ActivitySignup::getUserId, currentUserOpenid)
                    .ne(ActivitySignup::getStatus, "2"); // 排除已取消

            List<ActivitySignup> mySignups = activitySignupMapper.selectList(signupWrapper);

            Set<Long> joinedActivityIds = mySignups.stream()
                    .map(ActivitySignup::getActivityId)
                    .collect(Collectors.toSet());

            for (ActivityResponse resp : list) {
                if (joinedActivityIds.contains(resp.getActivityId())) {
                    resp.setIsJoined(true);
                }
            }
        }
        // ================= 核心修改结束 =================

        Page<ActivityResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(result, responsePage, "records");
        responsePage.setRecords(list);

        return responsePage;
    }

    private ActivityResponse convertToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        BeanUtils.copyProperties(activity, response);
        return response;
    }
}
