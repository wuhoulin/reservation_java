package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.controller.WeChatAuthController.WeChatUserInfo;
import com.microservice.skeleton.user.domain.dto.UserProfileDTO;
import com.microservice.skeleton.user.domain.entity.User.User;
import com.microservice.skeleton.user.domain.entity.User.UserWechat;
import com.microservice.skeleton.user.domain.vo.UserProfileVO;

public interface UserService extends IService<User> {

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据openid查找微信用户
     */
    UserWechat findWechatUserByOpenid(String openid);

    /**
     * 创建或更新微信用户
     */
    User createOrUpdateWechatUser(WeChatUserInfo wechatUserInfo);

    /**
     * 根据openid获取系统用户
     */
    User getUserByOpenid(String openid);


    UserProfileVO getUserProfile(String openid);

    void updateUserProfile(String openid, UserProfileDTO userProfileDTO);
}
