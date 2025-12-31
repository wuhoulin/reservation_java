package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.entity.RoomTime;
import com.microservice.skeleton.user.mapper.RoomTimeMapper;
import com.microservice.skeleton.user.service.RoomTimeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomTimeServiceImpl extends ServiceImpl<RoomTimeMapper, RoomTime> implements RoomTimeService {

    @Resource
    private RoomTimeMapper roomTimeMapper;

    /**
     * 查询指定教室的所有时间段
     */
    public List<RoomTime> getRoomAllTimePoints(Integer roomId) {
        return roomTimeMapper.selectByRoomId(roomId);
    }
}
