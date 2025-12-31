package com.microservice.skeleton.user.domain.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ActivityRequest {
    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String summary;
    private String description;
    private String organizer;

    @NotNull(message = "开始时间不能为空")
    private Date startTime;

    @NotNull(message = "结束时间不能为空")
    private Date endTime;

    private Date signupDeadline;
    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String coverImage;


    private Integer maxPeople;

    private String status;
    private String isRecommend;
    private String remark;
}
