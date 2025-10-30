package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.Request.RoomRequest;
import com.microservice.skeleton.user.domain.Response.RoomResponse;
import com.microservice.skeleton.user.domain.entity.Community;
import com.microservice.skeleton.user.domain.entity.Room;
import com.microservice.skeleton.user.domain.vo.RoomVO;
import com.microservice.skeleton.user.mapper.RoomMapper;
import com.microservice.skeleton.user.service.CommunityService;
import com.microservice.skeleton.user.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Autowired
    private CommunityService communityService;

    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        // 检查社区是否存在
        Community community = communityService.getById(request.getCommunityId());
        if (community == null) {
            throw new BusinessException("社区不存在");
        }

        // 检查教室名称是否已在该社区中存在
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getCommunityId, request.getCommunityId())
                   .eq(Room::getName, request.getName());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("该社区中已存在同名教室");
        }

        // 创建新教室
        Room room = new Room();
        BeanUtils.copyProperties(request, room);

        // 保存教室
        this.save(room);

        // 转换为响应对象
        RoomResponse response = new RoomResponse();
        BeanUtils.copyProperties(room, response);
        response.setCommunityId(room.getCommunityId());
        response.setCommunityName(community.getName());

        return response;
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Integer id, RoomRequest request) {
        // 检查教室是否存在
        Room room = this.getById(id);
        if (room == null) {
            throw new BusinessException("教室不存在");
        }

        // 检查社区是否存在
        Community community = communityService.getById(request.getCommunityId());
        if (community == null) {
            throw new BusinessException("社区不存在");
        }

        // 检查教室名称是否已在该社区中被其他教室使用
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getCommunityId, request.getCommunityId())
                   .eq(Room::getName, request.getName())
                   .ne(Room::getId, id);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("该社区中已存在同名教室");
        }

        // 更新教室信息
        BeanUtils.copyProperties(request, room);

        // 保存教室
        this.updateById(room);

        // 转换为响应对象
        RoomResponse response = new RoomResponse();
        BeanUtils.copyProperties(room, response);
        response.setCommunityId(room.getCommunityId());
        response.setCommunityName(community.getName());

        return response;
    }

    @Override
    @Transactional
    public void deleteRoom(Integer id) {
        // 检查教室是否存在
        if (!this.removeById(id)) {
            throw new BusinessException("教室不存在");
        }
    }

    @Override
    public RoomResponse getRoomById(Integer id) {
        // 检查教室是否存在
        Room room = this.getById(id);
        if (room == null) {
            throw new BusinessException("教室不存在");
        }

        // 获取社区信息
        Community community = communityService.getById(room.getCommunityId());

        // 转换为响应对象
        RoomResponse response = new RoomResponse();
        BeanUtils.copyProperties(room, response);
        response.setCommunityId(room.getCommunityId());
        response.setCommunityName(community.getName());

        return response;
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        // 获取所有教室
        List<Room> rooms = this.list();

        // 转换为响应对象列表
        return rooms.stream()
                .map(room -> {
                    Community community = communityService.getById(room.getCommunityId());
                    RoomResponse response = new RoomResponse();
                    BeanUtils.copyProperties(room, response);
                    response.setCommunityId(room.getCommunityId());
                    response.setCommunityName(community.getName());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<RoomResponse> getAllRooms(Page<Room> page) {
        // 分页获取所有教室
        Page<Room> roomPage = this.page(page);

        // 转换为响应对象分页
        Page<RoomResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(roomPage, responsePage, "records");

        List<RoomResponse> records = roomPage.getRecords().stream()
                .map(room -> {
                    Community community = communityService.getById(room.getCommunityId());
                    RoomResponse response = new RoomResponse();
                    BeanUtils.copyProperties(room, response);
                    response.setCommunityId(room.getCommunityId());
                    response.setCommunityName(community.getName());
                    return response;
                })
                .collect(Collectors.toList());

        responsePage.setRecords(records);

        return responsePage;
    }

    @Override
    public List<RoomResponse> getRoomsByCommunity(Integer communityId) {
        // 检查社区是否存在
        Community community = communityService.getById(communityId);
        if (community == null) {
            throw new BusinessException("社区不存在");
        }

        // 获取社区下的所有教室
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getCommunityId, communityId);
        List<Room> rooms = this.list(queryWrapper);

        // 转换为响应对象列表
        return rooms.stream()
                .map(room -> {
                    RoomResponse response = new RoomResponse();
                    BeanUtils.copyProperties(room, response);
                    response.setCommunityId(communityId);
                    response.setCommunityName(community.getName());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomVO> getRoomsByCommunityId(Integer communityId) {
        // 检查社区是否存在
        Community community = communityService.getById(communityId);
        if (community == null) {
            throw new BusinessException("社区不存在");
        }

        // 获取社区下的所有教室
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getCommunityId, communityId);
        List<Room> rooms = this.list(queryWrapper);

        // 转换为视图对象列表
        return rooms.stream()
                .map(room -> {
                    RoomVO vo = new RoomVO();
                    BeanUtils.copyProperties(room, vo);
                    vo.setCommunityId(communityId);
                    vo.setCommunityName(community.getName());
                    vo.setAvailable(room.getStatus());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponse> searchRooms(String keyword) {
        // 搜索教室
        List<Room> rooms = baseMapper.searchByKeyword(keyword);

        // 转换为响应对象列表
        return rooms.stream()
                .map(room -> {
                    Community community = communityService.getById(room.getCommunityId());
                    RoomResponse response = new RoomResponse();
                    BeanUtils.copyProperties(room, response);
                    response.setCommunityId(room.getCommunityId());
                    response.setCommunityName(community.getName());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponse> getAvailableRooms(Integer communityId, LocalDate date, Integer TimePointId) {
        // 检查社区是否存在
        Community community = communityService.getById(communityId);
        if (community == null) {
            throw new BusinessException("社区不存在");
        }

        // 获取可用教室
        List<Room> rooms = baseMapper.findAvailableRooms(communityId, date, TimePointId);

        // 转换为响应对象列表
        return rooms.stream()
                .map(room -> {
                    RoomResponse response = new RoomResponse();
                    BeanUtils.copyProperties(room, response);
                    response.setCommunityId(room.getCommunityId());
                    response.setCommunityName(community.getName());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RoomVO getRoomDetails(Integer id) {
        // 检查教室是否存在
        Room room = this.getById(id);
        if (room == null) {
            throw new BusinessException("教室不存在");
        }

        // 获取社区信息
        Community community = communityService.getById(room.getCommunityId());

        // 转换为视图对象
        RoomVO vo = new RoomVO();
        BeanUtils.copyProperties(room, vo);
        vo.setCommunityId(room.getCommunityId());
        vo.setCommunityName(community.getName());
        vo.setAvailable(room.getStatus());

        return vo;
    }
}
