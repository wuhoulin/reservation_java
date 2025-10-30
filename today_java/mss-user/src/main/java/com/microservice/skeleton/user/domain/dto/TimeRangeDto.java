package com.microservice.skeleton.user.domain.dto;

import lombok.Data;

@Data

public class TimeRangeDto {
    private String start;  // 格式 "08:00"
    private String end;    // 格式 "09:30"
    private String reservationNo;   // 这段时间对应的预约编号
    private String userId;          // 占用这个时段的用户
}
