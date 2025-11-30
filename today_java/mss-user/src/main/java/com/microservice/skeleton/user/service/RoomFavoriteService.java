package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Request.RoomFavoriteRequest;
import com.microservice.skeleton.user.domain.Response.RoomFavoriteResponse;
import java.util.List;

public interface RoomFavoriteService {

    /**
     * 添加收藏
     */
    Boolean addFavorite(String userId, RoomFavoriteRequest request);

    /**
     * 取消收藏
     */
    Boolean removeFavorite(String userId, Integer roomId);

    /**
     * 获取用户的收藏列表
     */
    List<RoomFavoriteResponse> getUserFavorites(String userId);

    /**
     * 分页获取用户的收藏列表
     */
    Page<RoomFavoriteResponse> getUserFavoritesPage(String userId, Integer current, Integer size);

    /**
     * 检查用户是否收藏了某个教室
     */
    Boolean isFavorited(String userId, Integer roomId);

    /**
     * 获取用户的收藏数量
     */
    Long getFavoriteCount(String userId);
}
