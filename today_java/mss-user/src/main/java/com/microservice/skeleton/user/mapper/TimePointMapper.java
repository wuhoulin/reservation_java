package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.microservice.skeleton.user.domain.entity.TimePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TimePointMapper extends BaseMapper<TimePoint> {

    @Select("SELECT tp.* FROM time_points tp " +
            "WHERE tp.status = 1 " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM reservations r " +
            "    WHERE r.room_id = #{roomId} " +
            "    AND r.reservation_date = #{date} " +
            "    AND (r.start_time_id = tp.id OR r.end_time_id = tp.id) " +
            "    AND r.status IN (0, 1, 4)" +
            ") " +
            "ORDER BY tp.point ASC")
    List<TimePoint> findAvailableTimePoints(@Param("roomId") Integer roomId, @Param("date") LocalDate date);

}
