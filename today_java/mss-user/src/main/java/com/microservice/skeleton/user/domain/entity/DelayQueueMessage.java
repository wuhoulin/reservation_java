package com.microservice.skeleton.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

// DelayQueueMessage.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DelayQueueMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reservationNo;      // 预约编号
    private Long reservationId;     // 预约ID
    private String userId;            // 用户ID
    private LocalDateTime executeTime; // 执行时间（预约结束时间）
    private String type;              // 消息类型：RESERVATION_COMPLETE
}
