package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.controller.WeChatAuthController.WeChatUserInfo;
import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.dto.UserProfileDTO;
import com.microservice.skeleton.user.domain.entity.User.User;
import com.microservice.skeleton.user.domain.entity.User.UserWechat;
import com.microservice.skeleton.user.domain.vo.UserProfileVO;
import com.microservice.skeleton.user.mapper.UserMapper;
import com.microservice.skeleton.user.mapper.UserWechatMapper;
import com.microservice.skeleton.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserWechatMapper userWechatMapper;

    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", username);
        queryWrapper.eq("del_flag", "0");
        return this.getOne(queryWrapper);
    }

    @Override
    public UserWechat findWechatUserByOpenid(String openid) {
        QueryWrapper<UserWechat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        return userWechatMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public User createOrUpdateWechatUser(WeChatUserInfo wechatUserInfo) {
        String openid = wechatUserInfo.getOpenid();

        // 检查是否已存在微信用户关联
        UserWechat userWechat = findWechatUserByOpenid(openid);

        if (userWechat != null) {
            // 更新现有微信用户信息
            return updateWechatUser(userWechat, wechatUserInfo);
        } else {
            // 创建新的微信用户
            return createNewWechatUser(wechatUserInfo);
        }
    }

    private User updateWechatUser(UserWechat userWechat, WeChatUserInfo wechatUserInfo) {
        // 更新微信用户信息
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

        userWechatMapper.updateById(userWechat);

        // 返回对应的系统用户
        return this.getById(userWechat.getUserId());
    }

    private User createNewWechatUser(WeChatUserInfo wechatUserInfo) {
        // 创建系统用户
        User user = User.builder()
                .userName("wx_" + wechatUserInfo.getOpenid().substring(0, 8)) // 生成唯一用户名
                .userType("01") // 微信用户
                .avatar(wechatUserInfo.getHeadimgurl())
                .sex(convertGender(wechatUserInfo.getSex()))
                .status("0") // 正常状态
                .delFlag("0") // 未删除
                .createBy("system")
                .createTime(LocalDateTime.now())
                .remark("微信授权自动创建")
                .build();

        this.save(user);

        // 创建微信用户关联
        UserWechat userWechat = new UserWechat();
        userWechat.setUserId(user.getUserId());
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

        userWechatMapper.insert(userWechat);

        log.info("创建新的微信用户: userId={}, openid={}", user.getUserId(), wechatUserInfo.getOpenid());
        return user;
    }

    @Override
    public User getUserByOpenid(String openid) {
        UserWechat userWechat = findWechatUserByOpenid(openid);
        if (userWechat != null) {
            return this.getById(userWechat.getUserId());
        }
        return null;
    }

    @Override
    public UserProfileVO getUserProfile(String openid) {
        UserWechat userWechat = findWechatUserByOpenid(openid);
        if (userWechat == null) {
            return null;
        }

        User user = this.getById(userWechat.getUserId());
        if (user == null) {
            return null;
        }

        UserProfileVO userProfileVO = new UserProfileVO();
        userProfileVO.setUserId(user.getUserId());
        userProfileVO.setNickname(userWechat.getNickname());
        userProfileVO.setAvatar(userWechat.getAvatarUrl());
        userProfileVO.setOpenid(userWechat.getOpenid());

        // 设置用户基本信息
        userProfileVO.setUserName(user.getUserName());
        userProfileVO.setStudentId(user.getStudentId());
        userProfileVO.setCollege(user.getCollege());
        userProfileVO.setMajor(user.getMajor());
        userProfileVO.setPhonenumber(user.getPhonenumber());

        return userProfileVO;
    }

    @Override
    @Transactional
    public void updateUserProfile(String openid, UserProfileDTO userProfileDTO) {
        UserWechat userWechat = findWechatUserByOpenid(openid);
        if (userWechat == null) {
            throw new BusinessException("用户不存在");
        }

        User user = this.getById(userWechat.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户信息
        user.setUserName(userProfileDTO.getUserName());
        user.setStudentId(userProfileDTO.getStudentId());
        user.setCollege(userProfileDTO.getCollege());
        user.setMajor(userProfileDTO.getMajor());
        user.setPhonenumber(userProfileDTO.getPhonenumber());
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy("user"); // 标记为用户自己更新

        boolean success = this.updateById(user);
        if (!success) {
            throw new BusinessException("更新用户信息失败");
        }

        log.info("用户个人信息更新成功: userId={}, openid={}", user.getUserId(), openid);
    }


    /**
     * 转换性别格式
     */
    private String convertGender(Integer wechatGender) {
        if (wechatGender == null) return "2"; // 未知
        switch (wechatGender) {
            case 1: return "0"; // 微信1=男，系统0=男
            case 2: return "1"; // 微信2=女，系统1=女
            default: return "2"; // 未知
        }
    }
}
