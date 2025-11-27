// UserWechatServiceImpl.java
package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.controller.WeChatAuthController.WeChatUserInfo;
import com.microservice.skeleton.user.domain.entity.User.UserWechat;
import com.microservice.skeleton.user.mapper.UserWechatMapper;
import com.microservice.skeleton.user.service.UserWechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserWechatServiceImpl extends ServiceImpl<UserWechatMapper, UserWechat> implements UserWechatService {

    @Override
    public UserWechat getByOpenid(String openid) {
        return this.baseMapper.selectByOpenid(openid);
    }

    @Override
    public UserWechat getByUserId(Long userId) {
        return this.baseMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public UserWechat createOrUpdateWechatUser(Long userId, WeChatUserInfo wechatUserInfo) {
        String openid = wechatUserInfo.getOpenid();

        // 检查是否已存在
        UserWechat userWechat = getByOpenid(openid);

        if (userWechat != null) {
            // 更新现有记录
            return updateWechatUser(userWechat, wechatUserInfo);
        } else {
            // 创建新记录
            return createNewWechatUser(userId, wechatUserInfo);
        }
    }

    private UserWechat updateWechatUser(UserWechat userWechat, WeChatUserInfo wechatUserInfo) {
        userWechat.setNickname(wechatUserInfo.getNickname());
        userWechat.setAvatarUrl(wechatUserInfo.getHeadimgurl());
        userWechat.setGender(wechatUserInfo.getSex());
        userWechat.setCountry(wechatUserInfo.getCountry());
        userWechat.setProvince(wechatUserInfo.getProvince());
        userWechat.setCity(wechatUserInfo.getCity());
        userWechat.setAccessToken(wechatUserInfo.getAccessToken());
        userWechat.setRefreshToken(wechatUserInfo.getRefreshToken());
        userWechat.setExpiresIn(wechatUserInfo.getExpiresIn());
        userWechat.setUpdateTime(LocalDateTime.now());

        this.updateById(userWechat);
        log.info("更新微信用户关联: id={}, openid={}", userWechat.getId(), userWechat.getOpenid());

        return userWechat;
    }

    private UserWechat createNewWechatUser(Long userId, WeChatUserInfo wechatUserInfo) {
        UserWechat userWechat = new UserWechat();
        userWechat.setUserId(userId);
        userWechat.setOpenid(wechatUserInfo.getOpenid());
        userWechat.setUnionid(wechatUserInfo.getUnionid());
        userWechat.setNickname(wechatUserInfo.getNickname());
        userWechat.setAvatarUrl(wechatUserInfo.getHeadimgurl());
        userWechat.setGender(wechatUserInfo.getSex());
        userWechat.setCountry(wechatUserInfo.getCountry());
        userWechat.setProvince(wechatUserInfo.getProvince());
        userWechat.setCity(wechatUserInfo.getCity());
        userWechat.setAccessToken(wechatUserInfo.getAccessToken());
        userWechat.setRefreshToken(wechatUserInfo.getRefreshToken());
        userWechat.setExpiresIn(wechatUserInfo.getExpiresIn());
        userWechat.setCreateTime(LocalDateTime.now());
        userWechat.setUpdateTime(LocalDateTime.now());

        this.save(userWechat);
        log.info("创建微信用户关联: id={}, userId={}, openid={}",
                userWechat.getId(), userId, userWechat.getOpenid());

        return userWechat;
    }

    @Override
    public Long getUserIdByOpenid(String openid) {
        UserWechat userWechat = getByOpenid(openid);
        return userWechat != null ? userWechat.getUserId() : null;
    }
}
