package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.Request.RoomFavoriteRequest;
import com.microservice.skeleton.user.domain.Response.RoomFavoriteResponse;
import com.microservice.skeleton.user.domain.entity.RoomFavorite;
import com.microservice.skeleton.user.mapper.RoomFavoriteMapper;
import com.microservice.skeleton.user.service.RoomFavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RoomFavoriteServiceImpl extends ServiceImpl<RoomFavoriteMapper, RoomFavorite> implements RoomFavoriteService {

    @Override
    @Transactional
    public Boolean addFavorite(String userId, RoomFavoriteRequest request) {
        // 检查是否已经收藏
        Boolean exists = this.baseMapper.existsByUserIdAndRoomId(userId, request.getRoomId());
        if (Boolean.TRUE.equals(exists)) {
            return true; // 已经收藏过了
        }

        // 添加收藏
        RoomFavorite favorite = new RoomFavorite();
        favorite.setUserId(userId);
        favorite.setRoomId(request.getRoomId());

        return this.save(favorite);
    }

    @Override
    @Transactional
    public Boolean removeFavorite(String userId, Integer roomId) {
        return this.baseMapper.deleteByUserIdAndRoomId(userId, roomId) > 0;
    }

    @Override
    public List<RoomFavoriteResponse> getUserFavorites(String userId) {
        return this.baseMapper.selectFavoritesByUserId(userId);
    }

    @Override
    public Page<RoomFavoriteResponse> getUserFavoritesPage(String userId, Integer current, Integer size) {
        Page<RoomFavoriteResponse> page = new Page<>(current, size);
        List<RoomFavoriteResponse> records = this.baseMapper.selectFavoritesByUserId(userId);

        page.setRecords(records);
        page.setTotal(records.size());
        return page;
    }

    @Override
    public Boolean isFavorited(String userId, Integer roomId) {
        return this.baseMapper.existsByUserIdAndRoomId(userId, roomId);
    }

    @Override
    public Long getFavoriteCount(String userId) {
        LambdaQueryWrapper<RoomFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomFavorite::getUserId, userId);
        return this.count(wrapper);
    }
}
