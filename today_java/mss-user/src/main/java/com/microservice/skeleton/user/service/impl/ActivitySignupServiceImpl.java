package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.Response.ActivitySignupResponse;
import com.microservice.skeleton.user.domain.entity.Activity;
import com.microservice.skeleton.user.domain.entity.ActivitySignup;
import com.microservice.skeleton.user.mapper.ActivityMapper;
import com.microservice.skeleton.user.mapper.ActivitySignupMapper;
import com.microservice.skeleton.user.service.ActivitySignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ActivitySignupServiceImpl extends ServiceImpl<ActivitySignupMapper, ActivitySignup> implements ActivitySignupService {

    @Autowired
    private ActivityMapper activityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinActivity(Long activityId, String userId) {
        // 查活动
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        // 校验状态
        if (!"0".equals(activity.getStatus())) {
            throw new RuntimeException("活动不可报名");
        }
        // 初步校验人数
        if (activity.getCurrentPeople() >= activity.getMaxPeople()) {
            throw new RuntimeException("名额已满");
        }

        // 查重
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId)
                .ne(ActivitySignup::getStatus, "2");
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("请勿重复报名");
        }

        // 插入记录
        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(activityId);
        signup.setUserId(userId);
        signup.setSignupTime(new Date());
        signup.setStatus("0");
        this.save(signup);

        // 原子更新人数 (解决并发超卖)
        LambdaUpdateWrapper<Activity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Activity::getActivityId, activityId)
                .lt(Activity::getCurrentPeople, activity.getMaxPeople()) // 再次校验人数
                .setSql("current_people = current_people + 1");

        boolean success = activityMapper.update(null, updateWrapper) > 0;
        if (!success) {
            throw new RuntimeException("名额已抢完");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSignup(Long activityId, String userId) {
        // 1. 查询报名记录
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId)
                .eq(ActivitySignup::getStatus, "0"); // 0=已报名
        ActivitySignup signup = this.getOne(wrapper);

        if (signup == null) {
            // 建议统一使用 ServiceException 或 BusinessException
            throw new RuntimeException("无有效报名记录");
        }

        Activity activity = activityMapper.selectActivityById(activityId);
        if (activity == null) {
            throw new BusinessException("关联的活动不存在");
        }

        Date now = new Date();
        Date startTime = activity.getStartTime();

        if (startTime != null) {
            long diff = startTime.getTime() - now.getTime();
            if (diff < 10800000) {
                throw new BusinessException("活动即将开始或已开始，无法取消报名（需提前3小时）");
            }
        }

        // 4. 更新报名状态为已取消 (2)
        signup.setStatus("2");
        this.updateById(signup);

        // 5. 原子操作归还名额 (current_people - 1)
        LambdaUpdateWrapper<Activity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Activity::getActivityId, activityId)
                .gt(Activity::getCurrentPeople, 0) // 双重保险，防止减成负数
                .setSql("current_people = current_people - 1");
        activityMapper.update(null, updateWrapper);
    }

    @Override
    public boolean checkIsJoined(Long activityId, String userId) {
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId)
                .ne(ActivitySignup::getStatus, "2");
        return this.count(wrapper) > 0;
    }

    @Override
    public Page<ActivitySignupResponse> getMySignupPage(Page<ActivitySignupResponse> page, String userId) {
        return baseMapper.selectMySignupPage(page, userId);
    }
}
