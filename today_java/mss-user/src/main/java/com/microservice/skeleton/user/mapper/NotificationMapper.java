// NotificationMapper.java
package com.microservice.skeleton.user.mapper;


import com.microservice.skeleton.user.domain.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NotificationMapper {
    Notification selectNotificationById(Long id);
    List<Notification> selectNotificationList(Notification notification);
    int insertNotification(Notification notification);
    int updateNotification(Notification notification);
    int deleteNotificationById(Long id);
    int deleteNotificationByIds(Long[] ids);

    // 用户相关操作
    int markAsRead(Long id);
    int markBatchAsRead(Long[] ids);
    int countUnreadByUserId(String userId);
    List<Notification> selectUserNotifications(@Param("userId") String userId,
                                              @Param("offset") Integer offset,
                                              @Param("limit") Integer limit);
    int deleteAllByUserId(String userId);
    int markAllAsReadByUserId(String userId);

    List<Notification> selectUnreadAuditNotifications(String userId);
}
