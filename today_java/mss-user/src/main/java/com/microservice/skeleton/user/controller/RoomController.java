package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.RoomRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.PageResult;
import com.microservice.skeleton.user.domain.Response.RoomResponse;
import com.microservice.skeleton.user.domain.entity.Room;
import com.microservice.skeleton.user.domain.vo.RoomVO;
import com.microservice.skeleton.user.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@Tag(name = "教室管理接口")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    @Operation(summary = "创建教室")
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新教室")
    @Parameter(name = "id", description = "教室ID", required = true)
    public ApiResponse<RoomResponse> updateRoom(@PathVariable Integer id, @Valid @RequestBody RoomRequest request) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除教室")
    @Parameter(name = "id", description = "教室ID", required = true)
    public ApiResponse<Void> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取教室详情")
    @Parameter(name = "id", description = "教室ID", required = true)
    public ApiResponse<RoomResponse> getRoom(@PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "获取所有教室")
    public ApiResponse<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> responses = roomService.getAllRooms();
        return ApiResponse.success(responses);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取教室")
    @Parameters({
            @Parameter(name = "current", description = "当前页码", required = true),
            @Parameter(name = "size", description = "每页数量", required = true)
    })
    public ApiResponse<PageResult<RoomResponse>> getAllRooms(@RequestParam(defaultValue = "1") Integer current,
                                                             @RequestParam(defaultValue = "10") Integer size) {
        Page<Room> page = new Page<>(current, size);
        Page<RoomResponse> responsePage = roomService.getAllRooms(page);

        PageResult<RoomResponse> pageResult = new PageResult<>();
        pageResult.setRecords(responsePage.getRecords());
        pageResult.setTotal(responsePage.getTotal());
        pageResult.setCurrent(responsePage.getCurrent());
        pageResult.setSize(responsePage.getSize());

        return ApiResponse.success(pageResult);
    }

    @GetMapping("/community/{communityId}")
    @Operation(summary = "获取社区下的所有教室")
    @Parameter(name = "communityId", description = "社区ID", required = true)
    public ApiResponse<List<RoomResponse>> getRoomsByCommunity(@PathVariable Integer communityId) {
        List<RoomResponse> responses = roomService.getRoomsByCommunity(communityId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索教室")
    @Parameter(name = "keyword", description = "关键字", required = true)
    public ApiResponse<List<RoomResponse>> searchRooms(@RequestParam String keyword) {
        List<RoomResponse> responses = roomService.searchRooms(keyword);
        return ApiResponse.success(responses);
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用教室")
    @Parameters({
            @Parameter(name = "communityId", description = "社区ID", required = true),
            @Parameter(name = "date", description = "日期", required = true),
            @Parameter(name = "TimePointId", description = "时间段ID", required = true)
    })
    public ApiResponse<List<RoomResponse>> getAvailableRooms(@RequestParam Integer communityId,
                                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                             @RequestParam Integer TimePointId) {
        List<RoomResponse> responses = roomService.getAvailableRooms(communityId, date, TimePointId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "获取教室详细信息")
    @Parameter(name = "id", description = "教室ID", required = true)
    public ApiResponse<RoomVO> getRoomDetail(@PathVariable Integer id) {
        RoomVO vo = roomService.getRoomDetails(id);
        return ApiResponse.success(vo);
    }
}
