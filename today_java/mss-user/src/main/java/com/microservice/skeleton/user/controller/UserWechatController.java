// UserWechatController.java
package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.entity.User.UserWechat;
import com.microservice.skeleton.user.service.UserWechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/wechat")
public class UserWechatController {

    @Autowired
    private UserWechatService userWechatService;


    @GetMapping("/openid/{openid}")
    public ApiResponse<UserWechat> getWechatUserByOpenid(@PathVariable String openid) {
        UserWechat userWechat = userWechatService.getByOpenid(openid);
        if (userWechat == null) {
            return ApiResponse.notFound("微信用户不存在");
        }
        return ApiResponse.success(userWechat);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<UserWechat> getWechatUserByUserId(@PathVariable Long userId) {
        UserWechat userWechat = userWechatService.getByUserId(userId);
        if (userWechat == null) {
            return ApiResponse.notFound("微信用户不存在");
        }
        return ApiResponse.success(userWechat);
    }



    @GetMapping("/check-bind/{openid}")
    public ApiResponse<Boolean> checkOpenidBound(@PathVariable String openid) {
        UserWechat userWechat = userWechatService.getByOpenid(openid);
        return ApiResponse.success(userWechat != null);
    }
}
