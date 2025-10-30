package com.microservice.skeleton.user.service.impl;

import com.microservice.skeleton.user.domain.entity.User.UserPenalty;
import com.microservice.skeleton.user.mapper.UserPenaltyMapper;
import com.microservice.skeleton.user.service.UserPenaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserPenaltyServiceImpl implements UserPenaltyService {
  @Autowired
  private UserPenaltyMapper penaltyMapper;
  @Override
  public boolean hasActivePenalty(String userId) {
    return !penaltyMapper.findActiveByUserId(userId).isEmpty();
  }
  @Override
  public void createPenalty(String userId, LocalDateTime start, LocalDateTime end) {
    UserPenalty p = new UserPenalty();
    p.setUserId(userId);
    p.setStartTime(start);
    p.setEndTime(end);
    penaltyMapper.insert(p);
  }
}
