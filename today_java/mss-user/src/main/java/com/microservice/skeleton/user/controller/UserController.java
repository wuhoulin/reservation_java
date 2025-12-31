package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.annotation.IgnoreSecurityCheck;
import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.dto.UserProfileDTO;
import com.microservice.skeleton.user.domain.entity.User.User;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.vo.UserProfileVO;
import com.microservice.skeleton.user.service.UserService;
import com.microservice.skeleton.user.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest; // 修改: javax -> jakarta
import jakarta.validation.Valid; // 修改: javax -> jakarta
import java.util.List;

@RestController
@Tag(name = "用户接口")
@RequestMapping("/api/user")
@CrossOrigin
@Slf4j
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public ApiResponse<User> getCurrentUserInfo() {
        String openid = UserContext.getCurrentOpenid();
        if (openid == null) {
            return ApiResponse.unauthorized(401,"用户未登录");
        }

        User user = userService.getUserByOpenid(openid);
        if (user == null) {
            return ApiResponse.notFound(500,"用户不存在");
        }

        return ApiResponse.success(user);
    }

    @Operation(summary = "获取用户个人信息")
    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> getUserProfile() {
        String openid = UserContext.getCurrentOpenid();
        if (openid == null) {
            return ApiResponse.unauthorized(401, "用户未登录");
        }

        UserProfileVO userProfile = userService.getUserProfile(openid);
        if (userProfile == null) {
            return ApiResponse.notFound(404, "用户不存在");
        }

        return ApiResponse.success(userProfile);
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public ApiResponse<Void> updateUserProfile(@Valid @RequestBody UserProfileDTO userProfileDTO) {
        String openid = UserContext.getCurrentOpenid();
        if (openid == null) {
            return ApiResponse.unauthorized(401, "用户未登录");
        }

        try {
            userService.updateUserProfile(openid, userProfileDTO);
            return ApiResponse.success();
        } catch (BusinessException e) {
            return ApiResponse.error(500, e.getMessage());
        } catch (Exception e) {
            log.error("更新用户个人信息失败: openid={}", openid, e);
            return ApiResponse.error(500, "更新个人信息失败");
        }
    }

    @Operation(summary = "获取所有用户列表")
    @GetMapping("/list")
    public ApiResponse<List<User>> getUserList() {
        List<User> users = userService.list();
        return ApiResponse.success(users);
    }

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{userId}")
    public ApiResponse<User> getUserById(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return ApiResponse.notFound("用户不存在");
        }
        return ApiResponse.success(user);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public ApiResponse<User> updateUser(@RequestBody User user) {
        boolean success = userService.updateById(user);
        if (success) {
            return ApiResponse.success(user);
        } else {
            return ApiResponse.error(500,"更新用户失败");
        }
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        boolean success = userService.removeById(userId);
        if (success) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error(500,"删除用户失败");
        }
    }

    @IgnoreSecurityCheck
    @Operation(summary = "检查用户名是否存在")
    @GetMapping("/check-username")
    public ApiResponse<Boolean> checkUsernameExists(@RequestParam String username) {
        User user = userService.findByUsername(username);
        return ApiResponse.success(user != null);
    }
}
