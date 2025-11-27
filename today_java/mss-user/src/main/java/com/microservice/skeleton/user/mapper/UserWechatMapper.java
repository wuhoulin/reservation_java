// UserWechatMapper.java
package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microservice.skeleton.user.domain.entity.User.UserWechat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserWechatMapper extends BaseMapper<UserWechat> {

    /**
     * 根据openid查询微信用户信息
     */
    UserWechat selectByOpenid(String openid);

    /**
     * 根据userId查询微信用户信息
     */
    UserWechat selectByUserId(Long userId);
}
