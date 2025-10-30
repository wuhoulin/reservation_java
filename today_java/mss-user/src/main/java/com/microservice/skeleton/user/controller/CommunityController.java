package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.microservice.skeleton.user.domain.Request.CommunityRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.CommunityResponse;
import com.microservice.skeleton.user.domain.Response.PageResult;
import com.microservice.skeleton.user.domain.entity.Community;
import com.microservice.skeleton.user.domain.vo.CommunityVO;
import com.microservice.skeleton.user.service.CommunityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/communities")
@Api(tags = "社区管理接口")
@CrossOrigin
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @PostMapping
    @ApiOperation("创建社区")
    public ApiResponse<CommunityResponse> createCommunity(@Valid @RequestBody CommunityRequest request) {
        CommunityResponse response = communityService.createCommunity(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新社区")
    @ApiImplicitParam(name = "id", value = "社区ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<CommunityResponse> updateCommunity(@PathVariable Integer id, @Valid @RequestBody CommunityRequest request) {
        CommunityResponse response = communityService.updateCommunity(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除社区")
    @ApiImplicitParam(name = "id", value = "社区ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<Void> deleteCommunity(@PathVariable Integer id) {
        communityService.deleteCommunity(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("获取社区详情")
    @ApiImplicitParam(name = "id", value = "社区ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<CommunityResponse> getCommunity(@PathVariable Integer id) {
        CommunityResponse response = communityService.getCommunityById(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    @ApiOperation("获取所有社区")
    public ApiResponse<List<CommunityResponse>> getAllCommunities() {
        List<CommunityResponse> responses = communityService.getAllCommunities();
        return ApiResponse.success(responses);
    }

    @GetMapping("/page")
    @ApiOperation("分页获取社区")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页码", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "size", value = "每页数量", required = true, dataType = "Integer", paramType = "query")
    })
    public ApiResponse<PageResult<CommunityResponse>> getAllCommunities(@RequestParam(defaultValue = "1") Integer current,
                                                                        @RequestParam(defaultValue = "10") Integer size) {
        Page<Community> page = new Page<>(current, size);
        Page<CommunityResponse> responsePage = communityService.getAllCommunities(page);

        PageResult<CommunityResponse> pageResult = new PageResult<>();
        pageResult.setRecords(responsePage.getRecords());
        pageResult.setTotal(responsePage.getTotal());
        pageResult.setCurrent(responsePage.getCurrent());
        pageResult.setSize(responsePage.getSize());

        return ApiResponse.success(pageResult);
    }

    @GetMapping("/search")
    @ApiOperation("搜索社区")
    @ApiImplicitParam(name = "keyword", value = "关键字", required = true, dataType = "String", paramType = "query")
    public ApiResponse<List<CommunityResponse>> searchCommunities(@RequestParam String keyword) {
        List<CommunityResponse> responses = communityService.searchCommunities(keyword);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}/rooms")
    @ApiOperation("获取社区及其教室")
    @ApiImplicitParam(name = "id", value = "社区ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<CommunityVO> getCommunityWithRooms(@PathVariable Integer id) {
        CommunityVO vo = communityService.getCommunityWithRooms(id);
        return ApiResponse.success(vo);
    }

    @GetMapping("/with-rooms")
    @ApiOperation("获取所有社区及其教室")
    public ApiResponse<List<CommunityVO>> getAllCommunitiesWithRooms() {
        List<CommunityVO> vos = communityService.getAllCommunitiesWithRooms();
        return ApiResponse.success(vos);
    }
}
