package com.microservice.skeleton.user.service;

import java.time.LocalDateTime;

public interface UserPenaltyService {
  boolean hasActivePenalty(String userId);
  void createPenalty(String userId, LocalDateTime start, LocalDateTime end);
}
