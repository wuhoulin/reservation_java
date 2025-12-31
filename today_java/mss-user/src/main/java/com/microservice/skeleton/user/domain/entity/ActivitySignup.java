package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("activity_signup")
public class ActivitySignup implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 报名记录ID */
    @TableId(value = "signup_id", type = IdType.AUTO)
    private Long signupId;

    /** 活动ID */
    private Long activityId;

    /** 用户ID (存储 OpenID, String类型) */
    private String userId;

    /** 报名时间 */
    private Date signupTime;

    /** 状态（0已报名 1已签到 2已取消） */
    private String status;
}
