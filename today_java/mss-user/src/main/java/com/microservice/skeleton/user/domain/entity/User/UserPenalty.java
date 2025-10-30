package com.microservice.skeleton.user.domain.entity.User;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPenalty {
  private Long id;
  private String userId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private LocalDateTime createdAt;
}
