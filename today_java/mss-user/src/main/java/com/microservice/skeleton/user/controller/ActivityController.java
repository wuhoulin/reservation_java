package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.ActivityRequest;
import com.microservice.skeleton.user.domain.Response.ActivityResponse;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.PageResult;
import com.microservice.skeleton.user.domain.entity.Activity;
import com.microservice.skeleton.user.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // 修改：javax -> jakarta

@RestController
@RequestMapping("/api/activities")
@Tag(name = "活动管理接口") // 修改：@Api -> @Tag
@CrossOrigin
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PostMapping
    @Operation(summary = "创建活动") // 修改：@ApiOperation -> @Operation
    public ApiResponse<ActivityResponse> createActivity(@Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.createActivity(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新活动")
    public ApiResponse<ActivityResponse> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.updateActivity(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除活动")
    @Parameter(name = "id", description = "活动ID", required = true) // 修改：@ApiImplicitParam -> @Parameter
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        activityService.removeById(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情(自动增加浏览量)")
    @Parameter(name = "id", description = "活动ID", required = true)
    public ApiResponse<ActivityResponse> getActivity(@PathVariable Long id) {
        ActivityResponse response = activityService.getActivityById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取活动列表")
    @Parameters({ // 修改：@ApiImplicitParams -> @Parameters
            @Parameter(name = "current", description = "当前页码", required = true),
            @Parameter(name = "size", description = "每页数量", required = true),
            @Parameter(name = "keyword", description = "搜索关键字", required = false),
            @Parameter(name = "status", description = "状态筛选(0报名中,1进行中,2已结束)", required = false)
    })
    public ApiResponse<PageResult<ActivityResponse>> getActivityPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Page<Activity> page = new Page<>(current, size);
        Page<ActivityResponse> responsePage = activityService.getActivityPage(page, keyword, status);

        PageResult<ActivityResponse> pageResult = new PageResult<>();
        pageResult.setRecords(responsePage.getRecords());
        pageResult.setTotal(responsePage.getTotal());
        pageResult.setCurrent(responsePage.getCurrent());
        pageResult.setSize(responsePage.getSize());

        return ApiResponse.success(pageResult);
    }
}
