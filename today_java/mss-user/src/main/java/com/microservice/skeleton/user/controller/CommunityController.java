package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.CommunityRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.CommunityResponse;
import com.microservice.skeleton.user.domain.Response.PageResult;
import com.microservice.skeleton.user.domain.entity.Community;
import com.microservice.skeleton.user.domain.vo.CommunityVO;
import com.microservice.skeleton.user.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/communities")
@Tag(name = "社区管理接口")
@CrossOrigin
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @PostMapping
    @Operation(summary = "创建社区") // 替换了 @ApiOperation
    public ApiResponse<CommunityResponse> createCommunity(@Valid @RequestBody CommunityRequest request) {
        CommunityResponse response = communityService.createCommunity(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新社区")
    @Parameter(name = "id", description = "社区ID", required = true) // 替换了 @ApiImplicitParam
    public ApiResponse<CommunityResponse> updateCommunity(@PathVariable Integer id, @Valid @RequestBody CommunityRequest request) {
        CommunityResponse response = communityService.updateCommunity(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCommunity(@PathVariable Integer id) {
        communityService.deleteCommunity(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取社区详情")
    @Parameter(name = "id", description = "社区ID", required = true)
    public ApiResponse<CommunityResponse> getCommunity(@PathVariable Integer id) {
        CommunityResponse response = communityService.getCommunityById(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<CommunityResponse>> getAllCommunities() {
        List<CommunityResponse> responses = communityService.getAllCommunities();
        return ApiResponse.success(responses);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取社区")
    @Parameters({
            @Parameter(name = "current", description = "当前页码", required = false),
            @Parameter(name = "size", description = "每页数量", required = false)
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
    @Operation(summary = "搜索社区")
    @Parameter(name = "keyword", description = "关键字", required = true)
    public ApiResponse<List<CommunityResponse>> searchCommunities(@RequestParam String keyword) {
        List<CommunityResponse> responses = communityService.searchCommunities(keyword);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}/rooms")
    @Operation(summary = "获取社区及其教室")
    @Parameter(name = "id", description = "社区ID", required = true)
    public ApiResponse<CommunityVO> getCommunityWithRooms(@PathVariable Integer id) {
        CommunityVO vo = communityService.getCommunityWithRooms(id);
        return ApiResponse.success(vo);
    }

    @GetMapping("/with-rooms")
    public ApiResponse<List<CommunityVO>> getAllCommunitiesWithRooms() {
        List<CommunityVO> vos = communityService.getAllCommunitiesWithRooms();
        return ApiResponse.success(vos);
    }
}
