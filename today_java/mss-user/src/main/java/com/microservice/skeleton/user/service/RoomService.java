package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.Request.RoomRequest;
import com.microservice.skeleton.user.domain.Response.RoomResponse;
import com.microservice.skeleton.user.domain.entity.Room;
import com.microservice.skeleton.user.domain.vo.RoomVO;


import java.time.LocalDate;
import java.util.List;

public interface RoomService extends IService<Room> {

    RoomResponse createRoom(RoomRequest request);

    RoomResponse updateRoom(Integer id, RoomRequest request);

    void deleteRoom(Integer id);

    RoomResponse getRoomById(Integer id);

    List<RoomResponse> getAllRooms();

    Page<RoomResponse> getAllRooms(Page<Room> page);

    List<RoomResponse> getRoomsByCommunity(Integer communityId);

    List<RoomVO> getRoomsByCommunityId(Integer communityId);

    List<RoomResponse> searchRooms(String keyword);

    List<RoomResponse> getAvailableRooms(Integer communityId, LocalDate date, Integer TimePointId);

    RoomVO getRoomDetails(Integer id);
}
