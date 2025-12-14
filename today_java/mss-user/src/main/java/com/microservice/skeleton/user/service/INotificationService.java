// INotificationService.java
package com.microservice.skeleton.user.service;


import com.microservice.skeleton.user.domain.entity.Notification;

import java.util.List;
import java.util.Map;

public interface INotificationService {
    Notification selectNotificationById(Long id);
    List<Notification> selectNotificationList(Notification notification);
    int insertNotification(Notification notification);
    int updateNotification(Notification notification);
    int deleteNotificationByIds(Long[] ids);
    int deleteNotificationById(Long id);

    // 用户端接口
    Map<String, Object> getUserNotifications(String userId, Integer pageNum, Integer pageSize);
    int getUnreadCount(String userId);
    int markAsRead(Long id);
    int markBatchAsRead(Long[] ids);
    int markAllAsRead(String userId);
    int deleteAllByUserId(String userId);

    // 发送通知
    void sendReservationNotification(String userId, Integer type,
                                     Object reservation, String reason);

    List<Notification> selectUnreadAuditNotifications(String userId);
}
