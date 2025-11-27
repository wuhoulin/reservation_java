package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microservice.skeleton.user.domain.entity.RoomTime;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoomTimeMapper extends BaseMapper<RoomTime> {
    /**
     * 根据教室ID查询所有时间段（按时间点升序）
     */
    List<RoomTime> selectByRoomId(@Param("roomId") Integer roomId);

    @Select("SELECT time_point_id FROM room_time WHERE room_id = #{roomId} AND status = 0")
    List<Integer> findUnavailableTimePointIdsByRoomId(@Param("roomId") Integer roomId);
}
