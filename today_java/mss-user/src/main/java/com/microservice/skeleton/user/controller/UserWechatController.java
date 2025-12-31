package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.entity.User.UserWechat;
import com.microservice.skeleton.user.service.UserWechatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "微信用户管理")
@RequestMapping("/user/wechat")
public class UserWechatController {

    @Autowired
    private UserWechatService userWechatService;

    @Operation(summary = "根据openid获取微信用户信息")
    @GetMapping("/openid/{openid}")
    public ApiResponse<UserWechat> getWechatUserByOpenid(@PathVariable String openid) {
        UserWechat userWechat = userWechatService.getByOpenid(openid);
        if (userWechat == null) {
            return ApiResponse.notFound("微信用户不存在");
        }
        return ApiResponse.success(userWechat);
    }

    @Operation(summary = "根据用户ID获取微信用户信息")
    @GetMapping("/user/{userId}")
    public ApiResponse<UserWechat> getWechatUserByUserId(@PathVariable Long userId) {
        UserWechat userWechat = userWechatService.getByUserId(userId);
        if (userWechat == null) {
            return ApiResponse.notFound("微信用户不存在");
        }
        return ApiResponse.success(userWechat);
    }

    @Operation(summary = "检查openid是否已绑定")
    @GetMapping("/check-bind/{openid}")
    public ApiResponse<Boolean> checkOpenidBound(@PathVariable String openid) {
        UserWechat userWechat = userWechatService.getByOpenid(openid);
        return ApiResponse.success(userWechat != null);
    }
}
