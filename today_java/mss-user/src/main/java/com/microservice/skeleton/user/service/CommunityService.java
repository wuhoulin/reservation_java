package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.Request.CommunityRequest;
import com.microservice.skeleton.user.domain.Response.CommunityResponse;
import com.microservice.skeleton.user.domain.entity.Community;
import com.microservice.skeleton.user.domain.vo.CommunityVO;


import java.util.List;

public interface CommunityService extends IService<Community> {

    CommunityResponse createCommunity(CommunityRequest request);

    CommunityResponse updateCommunity(Integer id, CommunityRequest request);

    void deleteCommunity(Integer id);

    CommunityResponse getCommunityById(Integer id);

    List<CommunityResponse> getAllCommunities();

    Page<CommunityResponse> getAllCommunities(Page<Community> page);

    List<CommunityResponse> searchCommunities(String keyword);

    CommunityVO getCommunityWithRooms(Integer id);

    List<CommunityVO> getAllCommunitiesWithRooms();
}
