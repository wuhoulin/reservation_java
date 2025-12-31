package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.RoomFavoriteRequest;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.Response.RoomFavoriteResponse;
import com.microservice.skeleton.user.service.RoomFavoriteService;
import com.microservice.skeleton.user.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "教室收藏接口")
public class RoomFavoriteController {

    private static final Logger log = LoggerFactory.getLogger(RoomFavoriteController.class);

    @Autowired
    private RoomFavoriteService roomFavoriteService;

    @PostMapping
    public ApiResponse<Boolean> addFavorite(@Valid @RequestBody RoomFavoriteRequest request) {
        String userId = UserContext.getCurrentOpenid();

        if (userId == null || userId.trim().isEmpty()) {
            log.error("收藏教室失败：用户未登录，请求参数：{}", request);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Boolean result = roomFavoriteService.addFavorite(userId, request);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "取消收藏")
    @Parameter(name = "roomId", description = "教室ID", required = true)
    public ApiResponse<Boolean> removeFavorite(@PathVariable Integer roomId) {
        String userId = UserContext.getCurrentOpenid();

        if (userId == null || userId.trim().isEmpty()) {
            log.error("取消收藏失败：用户未登录，教室ID：{}", roomId);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Boolean result = roomFavoriteService.removeFavorite(userId, roomId);
        return ApiResponse.success(result);
    }

    @GetMapping
    @Operation(summary = "获取用户的收藏列表")
    public ApiResponse<List<RoomFavoriteResponse>> getUserFavorites() {
        String userId = UserContext.getCurrentOpenid();

        if (userId == null || userId.trim().isEmpty()) {
            log.error("获取用户收藏列表失败：用户未登录");
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        List<RoomFavoriteResponse> favorites = roomFavoriteService.getUserFavorites(userId);
        return ApiResponse.success(favorites);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取用户的收藏列表")
    @Parameters({
            @Parameter(name = "current", description = "当前页码", required = true),
            @Parameter(name = "size", description = "每页数量", required = true)
    })
    public ApiResponse<Page<RoomFavoriteResponse>> getUserFavoritesPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        String userId = UserContext.getCurrentOpenid();

        if (userId == null || userId.trim().isEmpty()) {
            log.error("分页获取用户收藏列表失败：用户未登录，页码：{}，每页数量：{}", current, size);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Page<RoomFavoriteResponse> page = roomFavoriteService.getUserFavoritesPage(userId, current, size);
        return ApiResponse.success(page);
    }

    @GetMapping("/check/{roomId}")
    @Operation(summary = "检查是否收藏了某个教室")
    @Parameter(name = "roomId", description = "教室ID", required = true)
    public ApiResponse<Boolean> isFavorited(@PathVariable Integer roomId) {
        String userId = UserContext.getCurrentOpenid();

        if (userId == null || userId.trim().isEmpty()) {
            log.error("检查教室收藏状态失败：用户未登录，教室ID：{}", roomId);
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Boolean isFavorited = roomFavoriteService.isFavorited(userId, roomId);
        return ApiResponse.success(isFavorited);
    }

    @GetMapping("/count")
    public ApiResponse<Long> getFavoriteCount() {
        String userId = UserContext.getCurrentOpenid();

        if (userId == null || userId.trim().isEmpty()) {
            log.error("获取用户收藏数量失败：用户未登录");
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        Long count = roomFavoriteService.getFavoriteCount(userId);
        return ApiResponse.success(count);
    }
}
