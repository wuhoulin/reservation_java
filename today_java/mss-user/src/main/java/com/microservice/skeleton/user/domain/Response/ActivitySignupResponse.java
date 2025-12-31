package com.microservice.skeleton.user.domain.Response;

import lombok.Data;
import java.util.Date;

@Data
public class ActivitySignupResponse {
    // --- 报名表数据 ---
    private Long signupId;
    private Long activityId;
    private String userId;
    private Date signupTime;
    private String status;
    private String activityTitle;
    private String activityLocation;
    private Date activityStartTime;
    private String activityCover;
    private String activityStatus; // 活动本身的状态
}
