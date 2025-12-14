package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.RoomFavoriteRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.RoomFavoriteResponse;
import com.microservice.skeleton.user.service.RoomFavoriteService;
import com.microservice.skeleton.user.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Api(tags = "教室收藏接口")
public class RoomFavoriteController {

    private static final Logger log = LoggerFactory.getLogger(RoomFavoriteController.class);

    @Autowired
    private RoomFavoriteService roomFavoriteService;

    @PostMapping
    @ApiOperation("收藏教室")
    public ApiResponse<Boolean> addFavorite(@Valid @RequestBody RoomFavoriteRequest request) {
        String userId = UserContext.getCurrentOpenid();

        // 关键修改：移除测试用默认值，改为返回未登录错误
        if (userId == null || userId.trim().isEmpty()) {
            log.error("收藏教室失败：用户未登录，请求参数：{}", request);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Boolean result = roomFavoriteService.addFavorite(userId, request);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{roomId}")
    @ApiOperation("取消收藏")
    @ApiImplicitParam(name = "roomId", value = "教室ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<Boolean> removeFavorite(@PathVariable Integer roomId) {
        String userId = UserContext.getCurrentOpenid();

        // 关键修改：移除测试用默认值，改为返回未登录错误
        if (userId == null || userId.trim().isEmpty()) {
            log.error("取消收藏失败：用户未登录，教室ID：{}", roomId);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Boolean result = roomFavoriteService.removeFavorite(userId, roomId);
        return ApiResponse.success(result);
    }

    @GetMapping
    @ApiOperation("获取用户的收藏列表")
    public ApiResponse<List<RoomFavoriteResponse>> getUserFavorites() {
        String userId = UserContext.getCurrentOpenid();

        // 关键修改：移除测试用默认值，改为返回未登录错误
        if (userId == null || userId.trim().isEmpty()) {
            log.error("获取用户收藏列表失败：用户未登录");
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        List<RoomFavoriteResponse> favorites = roomFavoriteService.getUserFavorites(userId);
        return ApiResponse.success(favorites);
    }

    @GetMapping("/page")
    @ApiOperation("分页获取用户的收藏列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页码", required = true, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = true, dataType = "Integer", paramType = "query")
    })
    public ApiResponse<Page<RoomFavoriteResponse>> getUserFavoritesPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        String userId = UserContext.getCurrentOpenid();

        // 关键修改：移除测试用默认值，改为返回未登录错误
        if (userId == null || userId.trim().isEmpty()) {
            log.error("分页获取用户收藏列表失败：用户未登录，页码：{}，每页数量：{}", current, size);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Page<RoomFavoriteResponse> page = roomFavoriteService.getUserFavoritesPage(userId, current, size);
        return ApiResponse.success(page);
    }

    @GetMapping("/check/{roomId}")
    @ApiOperation("检查是否收藏了某个教室")
    @ApiImplicitParam(name = "roomId", value = "教室ID", required = true, dataType = "Integer", paramType = "path")
    public ApiResponse<Boolean> isFavorited(@PathVariable Integer roomId) {
        String userId = UserContext.getCurrentOpenid();

        // 关键修改：移除测试用默认值，改为返回未登录错误
        if (userId == null || userId.trim().isEmpty()) {
            log.error("检查教室收藏状态失败：用户未登录，教室ID：{}", roomId);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Boolean isFavorited = roomFavoriteService.isFavorited(userId, roomId);
        return ApiResponse.success(isFavorited);
    }

    @GetMapping("/count")
    @ApiOperation("获取用户的收藏数量")
    public ApiResponse<Long> getFavoriteCount() {
        String userId = UserContext.getCurrentOpenid();

        // 关键修改：移除测试用默认值，改为返回未登录错误
        if (userId == null || userId.trim().isEmpty()) {
            log.error("获取用户收藏数量失败：用户未登录");
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Long count = roomFavoriteService.getFavoriteCount(userId);
        return ApiResponse.success(count);
    }
}
