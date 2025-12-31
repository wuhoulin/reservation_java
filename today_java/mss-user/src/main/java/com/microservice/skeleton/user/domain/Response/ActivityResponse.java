package com.microservice.skeleton.user.domain.Response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ActivityResponse {
    private Long activityId;
    private String title;
    private String summary;
    private String description;
    private String organizer;
    private Date startTime;
    private Date endTime;
    private Date signupDeadline;
    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String coverImage;
    private Integer maxPeople;
    private Integer currentPeople;
    private Long viewCount;
    private String status;
    private String isRecommend;
    private Date createTime;
    private Boolean isJoined = false;
}
