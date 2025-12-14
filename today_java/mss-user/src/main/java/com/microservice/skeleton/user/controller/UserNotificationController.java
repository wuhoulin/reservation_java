package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Response.ApiResponse;
import com.microservice.skeleton.user.domain.entity.Notification;
import com.microservice.skeleton.user.service.INotificationService;
import com.microservice.skeleton.user.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 用户通知控制器
 */
@RestController
@RequestMapping("/user/notification")
public class UserNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(UserNotificationController.class);

    @Autowired
    private INotificationService notificationService;

    /**
     * 获取用户通知列表
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        // 从UserContext获取用户ID（与其他接口保持一致）
        String userId = getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        // 校验分页参数合法性
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        try {
            Map<String, Object> result = notificationService.getUserNotifications(userId, pageNum, pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取通知列表失败，用户ID：{}", userId, e);
            return ApiResponse.error(500, "获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public ApiResponse<Integer> getUnreadCount() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        try {
            int count = notificationService.getUnreadCount(userId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            logger.error("获取未读数量失败，用户ID：{}", userId, e);
            return ApiResponse.error(500, "获取未读数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读的审核结果通知（只返回审核通过和拒绝的通知）
     */
    @GetMapping("/unread/audit")
    public ApiResponse<Map<String, Object>> getUnreadAuditNotifications() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        try {
            // 查询用户未读的审核通知（type=2:审核通过, type=3:审核拒绝）
            List<Notification> notifications = notificationService.selectUnreadAuditNotifications(userId);

            // 按创建时间倒序
            notifications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            Map<String, Object> result = new HashMap<>();
            result.put("notifications", notifications);
            result.put("count", notifications.size());
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取未读审核通知失败，用户ID：{}", userId, e);
            return ApiResponse.error(500, "获取未读审核通知失败: " + e.getMessage());
        }
    }

    /**
     * 标记单个通知为已读
     */
    @PostMapping("/mark/{id}")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        // 校验通知ID合法性
        if (id == null || id <= 0) {
            return ApiResponse.error(400, "通知ID格式错误");
        }

        try {
            // 验证通知属于当前用户
            Notification notification = notificationService.selectNotificationById(id);
            if (notification == null) {
                return ApiResponse.error(404, "通知不存在");
            }
            if (!userId.equals(notification.getUserId())) {
                logger.warn("用户{}尝试操作不属于自己的通知{}", userId, id);
                return ApiResponse.error(403, "无权操作该通知");
            }

            int result = notificationService.markAsRead(id);
            if (result > 0) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error(500, "标记已读失败");
            }
        } catch (Exception e) {
            logger.error("标记通知{}为已读失败，用户ID：{}", id, userId, e);
            return ApiResponse.error(500, "标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/mark/all")
    public ApiResponse<Void> markAllAsRead() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        try {
            int result = notificationService.markAllAsRead(userId);
            // 即使没有未读通知（result=0）也返回成功，避免前端误解
            return ApiResponse.success();
        } catch (Exception e) {
            logger.error("标记用户{}所有通知为已读失败", userId, e);
            return ApiResponse.error(500, "标记全部已读失败: " + e.getMessage());
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        // 校验通知ID合法性
        if (id == null || id <= 0) {
            return ApiResponse.error(400, "通知ID格式错误");
        }

        try {
            // 验证通知属于当前用户
            Notification notification = notificationService.selectNotificationById(id);
            if (notification == null) {
                return ApiResponse.error(404, "通知不存在或已被删除");
            }
            if (!userId.equals(notification.getUserId())) {
                logger.warn("用户{}尝试删除不属于自己的通知{}", userId, id);
                return ApiResponse.error(403, "无权操作该通知");
            }

            int result = notificationService.deleteNotificationById(id);
            if (result > 0) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error(404, "通知不存在或已被删除");
            }
        } catch (Exception e) {
            logger.error("删除通知{}失败，用户ID：{}", id, userId, e);
            return ApiResponse.error(500, "删除通知失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有通知
     */
    @DeleteMapping("/clear/all")
    public ApiResponse<Void> clearAll() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录或身份验证失败，请重新登录后再试");
        }

        try {
            int result = notificationService.deleteAllByUserId(userId);
            // 即使没有通知（result=0）也返回成功
            return ApiResponse.success();
        } catch (Exception e) {
            logger.error("清空用户{}所有通知失败", userId, e);
            return ApiResponse.error(500, "清空通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID（统一从UserContext获取，与其他接口保持一致）
     * 移除硬编码，使用项目统一的用户上下文工具类
     */
    private String getCurrentUserId() {
        try {
            String userId = UserContext.getCurrentOpenid();
            // 补充空值校验，过滤空字符串/空格
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("获取用户ID失败：UserContext中openid为空");
                return null;
            }
            return userId.trim();
        } catch (Exception e) {
            logger.error("从UserContext获取用户ID失败", e);
            return null;
        }
    }
}
