package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.RoomRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.PageResult;
import com.microservice.skeleton.user.domain.Response.RoomResponse;
import com.microservice.skeleton.user.domain.entity.Room;
import com.microservice.skeleton.user.domain.vo.RoomVO;
import com.microservice.skeleton.user.service.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@Api(tags = "教室管理接口")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    @ApiOperation("创建教室")
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新教室")
    @ApiImplicitParam(name = "id", value = "教室ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<RoomResponse> updateRoom(@PathVariable Integer id, @Valid @RequestBody RoomRequest request) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除教室")
    @ApiImplicitParam(name = "id", value = "教室ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<Void> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("获取教室详情")
    @ApiImplicitParam(name = "id", value = "教室ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<RoomResponse> getRoom(@PathVariable Integer id) {
        RoomResponse response = roomService.getRoomById(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    @ApiOperation("获取所有教室")
    public ApiResponse<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> responses = roomService.getAllRooms();
        return ApiResponse.success(responses);
    }

    @GetMapping("/page")
    @ApiOperation("分页获取教室")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页码", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "size", value = "每页数量", required = true, dataType = "Integer", paramType = "query")
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
    @ApiOperation("获取社区下的所有教室")
    @ApiImplicitParam(name = "communityId", value = "社区ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<List<RoomResponse>> getRoomsByCommunity(@PathVariable Integer communityId) {
        List<RoomResponse> responses = roomService.getRoomsByCommunity(communityId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/search")
    @ApiOperation("搜索教室")
    @ApiImplicitParam(name = "keyword", value = "关键字", required = true, dataType = "String", paramType = "query")
    public ApiResponse<List<RoomResponse>> searchRooms(@RequestParam String keyword) {
        List<RoomResponse> responses = roomService.searchRooms(keyword);
        return ApiResponse.success(responses);
    }

    @GetMapping("/available")
    @ApiOperation("获取可用教室")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "communityId", value = "社区ID", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "date", value = "日期", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "TimePointId", value = "时间段ID", required = true, dataType = "Integer", paramType = "query")
    })
    public ApiResponse<List<RoomResponse>> getAvailableRooms(@RequestParam Integer communityId,
                                                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                            @RequestParam Integer TimePointId) {
        List<RoomResponse> responses = roomService.getAvailableRooms(communityId, date, TimePointId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}/detail")
    @ApiOperation("获取教室详细信息")
    @ApiImplicitParam(name = "id", value = "教室ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<RoomVO> getRoomDetail(@PathVariable Integer id) {
        RoomVO vo = roomService.getRoomDetails(id);
        return ApiResponse.success(vo);
    }
}
