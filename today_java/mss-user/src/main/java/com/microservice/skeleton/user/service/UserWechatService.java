// UserWechatService.java
package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.controller.WeChatAuthController.WeChatUserInfo;
import com.microservice.skeleton.user.domain.entity.User.UserWechat;

public interface UserWechatService extends IService<UserWechat> {

    /**
     * 根据openid获取微信用户信息
     */
    UserWechat getByOpenid(String openid);

    /**
     * 根据userId获取微信用户信息
     */
    UserWechat getByUserId(Long userId);

    /**
     * 创建或更新微信用户关联
     */
    UserWechat createOrUpdateWechatUser(Long userId, WeChatUserInfo wechatUserInfo);

    /**
     * 根据openid获取系统用户ID
     */
    Long getUserIdByOpenid(String openid);
}
