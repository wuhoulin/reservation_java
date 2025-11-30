package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microservice.skeleton.user.domain.entity.RoomFavorite;
import com.microservice.skeleton.user.domain.Response.RoomFavoriteResponse;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RoomFavoriteMapper extends BaseMapper<RoomFavorite> {

    /**
     * 获取用户的收藏列表
     */
    List<RoomFavoriteResponse> selectFavoritesByUserId(@Param("userId") String userId);

    /**
     * 检查用户是否收藏了某个教室
     */
    Boolean existsByUserIdAndRoomId(@Param("userId") String userId, @Param("roomId") Integer roomId);

    /**
     * 删除用户的收藏
     */
    int deleteByUserIdAndRoomId(@Param("userId") String userId, @Param("roomId") Integer roomId);
}
