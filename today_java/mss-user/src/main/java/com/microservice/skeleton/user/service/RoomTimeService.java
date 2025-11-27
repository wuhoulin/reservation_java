package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.entity.RoomTime;

import java.util.List;

public interface RoomTimeService extends IService<RoomTime> {
    List<RoomTime> getRoomAllTimePoints(Integer roomId);
}
