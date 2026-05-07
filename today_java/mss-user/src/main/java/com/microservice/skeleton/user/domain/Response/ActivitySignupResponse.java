package com.microservice.skeleton.user.domain.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ActivitySignupResponse {
    // --- 原有字段 ---
    private Long signupId;
    private Long activityId;
    private String userId;
    private Date signupTime;
    private String status;
    private String activityTitle;
    private String activityLocation;
    private String activityCover;
    private String activityStatus;

    // --- 🔥 新增：活动结束时间字段 ---
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date activityEndTime;

    // --- 修改：为开始时间添加格式化注解，确保显示年份 ---
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date activityStartTime;

    // --- 进度条需要的字段 ---
    private Integer maxPeople;
    private Integer currentPeople;
}
