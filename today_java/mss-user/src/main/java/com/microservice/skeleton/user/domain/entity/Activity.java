package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("activity")
public class Activity {

    @TableId(value = "activity_id", type = IdType.AUTO)
    private Long activityId;

    private String title;

    private String summary;

    private String description;

    private String organizer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date signupDeadline;

    private String location;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String coverImage;

    private Integer maxPeople;

    private Integer currentPeople;

    private Long viewCount;

    // 状态（0报名中 1进行中 2已结束 3已取消）
    private String status;


    private String isRecommend;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private String remark;
}
