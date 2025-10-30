package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.Request.CommunityRequest;
import com.microservice.skeleton.user.domain.Response.CommunityResponse;
import com.microservice.skeleton.user.domain.entity.Community;
import com.microservice.skeleton.user.domain.vo.CommunityVO;
import com.microservice.skeleton.user.domain.vo.RoomVO;
import com.microservice.skeleton.user.mapper.CommunityMapper;
import com.microservice.skeleton.user.service.CommunityService;
import com.microservice.skeleton.user.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunityServiceImpl extends ServiceImpl<CommunityMapper, Community> implements CommunityService {

    @Autowired
    private RoomService roomService;
    @Autowired
    private CommunityMapper communityMapper;

    @Override
    @Transactional
    public CommunityResponse createCommunity(CommunityRequest request) {
        // 检查社区名称是否已存在
        LambdaQueryWrapper<Community> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Community::getName, request.getName());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("社区名称已存在");
        }

        // 创建新社区
        Community community = new Community();
        BeanUtils.copyProperties(request, community);

        // 保存社区
        this.save(community);

        // 转换为响应对象
        CommunityResponse response = new CommunityResponse();
        BeanUtils.copyProperties(community, response);

        return response;
    }

    @Override
    @Transactional
    public CommunityResponse updateCommunity(Integer id, CommunityRequest request) {
        // 检查社区是否存在
        Community community = this.getById(id);
        if (community == null) {
            throw new BusinessException("社区不存在");
        }

        // 检查社区名称是否已被其他社区使用
        LambdaQueryWrapper<Community> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Community::getName, request.getName())
                   .ne(Community::getId, id);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("社区名称已存在");
        }

        // 更新社区信息
        BeanUtils.copyProperties(request, community);

        // 保存社区
        this.updateById(community);

        // 转换为响应对象
        CommunityResponse response = new CommunityResponse();
        BeanUtils.copyProperties(community, response);

        return response;
    }

    @Override
    @Transactional
    public void deleteCommunity(Integer id) {
        // 检查社区是否存在
        if (!this.removeById(id)) {
            throw new BusinessException("社区不存在");
        }
    }

    @Override
    public CommunityResponse getCommunityById(Integer id) {
        // 检查社区是否存在
        Community community = this.getById(id);
        if (community == null) {
            throw new BusinessException("社区不存在");
        }

        // 转换为响应对象
        CommunityResponse response = new CommunityResponse();
        BeanUtils.copyProperties(community, response);

        return response;
    }

    @Override
    public List<CommunityResponse> getAllCommunities() {
        // 获取所有社区
        List<Community> communities = communityMapper.findAll();

        // 转换为响应对象列表
        return communities.stream()
                .map(community -> {
                    CommunityResponse response = new CommunityResponse();
                    BeanUtils.copyProperties(community, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommunityResponse> getAllCommunities(Page<Community> page) {
        // 分页获取所有社区
        Page<Community> communityPage = this.page(page);

        // 转换为响应对象分页
        Page<CommunityResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(communityPage, responsePage, "records");

        List<CommunityResponse> records = communityPage.getRecords().stream()
                .map(community -> {
                    CommunityResponse response = new CommunityResponse();
                    BeanUtils.copyProperties(community, response);
                    return response;
                })
                .collect(Collectors.toList());

        responsePage.setRecords(records);

        return responsePage;
    }

    @Override
    public List<CommunityResponse> searchCommunities(String keyword) {
        // 搜索社区
        List<Community> communities = baseMapper.searchByKeyword(keyword);

        // 转换为响应对象列表
        return communities.stream()
                .map(community -> {
                    CommunityResponse response = new CommunityResponse();
                    BeanUtils.copyProperties(community, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CommunityVO getCommunityWithRooms(Integer id) {
        // 检查社区是否存在
        Community community = this.getById(id);
        if (community == null) {
            throw new BusinessException("社区不存在");
        }

        // 获取社区下的所有教室
        List<RoomVO> rooms = roomService.getRoomsByCommunityId(id);

        // 转换为视图对象
        CommunityVO vo = new CommunityVO();
        BeanUtils.copyProperties(community, vo);
        vo.setRooms(rooms);

        return vo;
    }

    @Override
    public List<CommunityVO> getAllCommunitiesWithRooms() {
        // 获取所有社区
        List<Community> communities = this.list();

        // 转换为视图对象列表
        return communities.stream()
                .map(community -> {
                    CommunityVO vo = new CommunityVO();
                    BeanUtils.copyProperties(community, vo);

                    // 获取社区下的所有教室
                    List<RoomVO> rooms = roomService.getRoomsByCommunityId(community.getId());
                    vo.setRooms(rooms);

                    return vo;
                })
                .collect(Collectors.toList());
    }
}
