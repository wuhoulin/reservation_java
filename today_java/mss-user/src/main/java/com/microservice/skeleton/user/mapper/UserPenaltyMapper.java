package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microservice.skeleton.user.domain.entity.User.UserPenalty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserPenaltyMapper extends BaseMapper<UserPenalty> {
  /** 查找某用户所有“未过期”惩罚 */
  @Select("SELECT * FROM user_penalty WHERE user_id = #{userId} AND end_time > NOW()")
  List<UserPenalty> findActiveByUserId(String userId);
}
