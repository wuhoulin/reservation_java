package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.microservice.skeleton.user.domain.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    @Select("SELECT * FROM rooms WHERE name LIKE CONCAT('%', #{keyword}, '%')")
    List<Room> searchByKeyword(@Param("keyword") String keyword);

    @Select("SELECT r.* FROM rooms r WHERE r.community_id = #{communityId} AND r.id NOT IN " +
           "(SELECT res.room_id FROM reservations res WHERE res.reservation_date = #{date} AND res.time_slot_id = #{TimePointId} AND res.status IN (0, 1, 4))")
    List<Room> findAvailableRooms(@Param("communityId") Integer communityId, @Param("date") LocalDate date, @Param("TimePointId") Integer TimePointId);
}
