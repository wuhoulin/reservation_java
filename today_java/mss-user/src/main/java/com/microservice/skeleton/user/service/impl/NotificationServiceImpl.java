// NotificationServiceImpl.java 补充完整的方法
package com.microservice.skeleton.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.microservice.skeleton.user.domain.entity.Notification;
import com.microservice.skeleton.user.domain.entity.Reservation;
import com.microservice.skeleton.user.mapper.NotificationMapper;
import com.microservice.skeleton.user.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements INotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public Notification selectNotificationById(Long id) {
        return notificationMapper.selectNotificationById(id);
    }

    @Override
    public List<Notification> selectNotificationList(Notification notification) {
        return notificationMapper.selectNotificationList(notification);
    }

    @Override
    public int insertNotification(Notification notification) {
        notification.setCreatedAt(new Date());
        notification.setUpdatedAt(new Date());
        return notificationMapper.insertNotification(notification);
    }

    @Override
    public int updateNotification(Notification notification) {
        notification.setUpdatedAt(new Date());
        return notificationMapper.updateNotification(notification);
    }

    @Override
    public int deleteNotificationByIds(Long[] ids) {
        return notificationMapper.deleteNotificationByIds(ids);
    }

    @Override
    public int deleteNotificationById(Long id) {
        return notificationMapper.deleteNotificationById(id);
    }

    @Override
    public Map<String, Object> getUserNotifications(String userId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 50) pageSize = 20;

        int offset = (pageNum - 1) * pageSize;
        List<Notification> list = notificationMapper.selectUserNotifications(userId, offset, pageSize);
        int unreadCount = getUnreadCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("notifications", list);
        result.put("unreadCount", unreadCount);
        result.put("total", list.size());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    @Override
    public int getUnreadCount(String userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public int markAsRead(Long id) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setIsRead(1);
        notification.setReadTime(new Date());
        return notificationMapper.updateNotification(notification);
    }

    @Override
    public int markBatchAsRead(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += markAsRead(id);
        }
        return count;
    }

    @Override
    public int markAllAsRead(String userId) {
        return notificationMapper.markAllAsReadByUserId(userId);
    }

    @Override
    public int deleteAllByUserId(String userId) {
        return notificationMapper.deleteAllByUserId(userId);
    }

    @Override
    @Transactional
    public void sendReservationNotification(String userId, Integer type,
                                            Object reservationObj, String reason) {
        if (reservationObj == null) return;

        Reservation reservation = (Reservation) reservationObj;
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setRelatedType(1); // 预约类型
        notification.setReservationNo(reservation.getReservationNo());
        notification.setRoomId(reservation.getRoomId());

        Map<String, Object> extraData = new HashMap<>();

        switch (type) {
            case 1: // 预约成功
                notification.setTitle("预约成功");
                notification.setContent(String.format("您的预约【%s】已提交成功，预约编号：%s，等待管理员审核",
                        reservation.getActivityName(), reservation.getReservationNo()));
                notification.setType(1);
                break;

            case 2: // 审核通过
                notification.setTitle("预约审核通过");
                notification.setContent(String.format("您的预约【%s】已通过审核，预约编号：%s，请按时使用教室",
                        reservation.getActivityName(), reservation.getReservationNo()));
                notification.setType(2);
                break;

            case 3: // 审核拒绝
                notification.setTitle("预约审核未通过");
                notification.setContent(String.format("您的预约【%s】未通过审核，预约编号：%s。原因：%s",
                        reservation.getActivityName(), reservation.getReservationNo(),
                        reason != null ? reason : "请查看详情"));
                notification.setType(3);
                extraData.put("rejectReason", reason);
                break;

            case 4: // 预约取消
                notification.setTitle("预约已取消");
                notification.setContent(String.format("您的预约【%s】已取消，预约编号：%s",
                        reservation.getActivityName(), reservation.getReservationNo()));
                notification.setType(4);
                break;

            case 5: // 预约完成
                notification.setTitle("预约已完成");
                notification.setContent(String.format("您的预约【%s】已完成，预约编号：%s",
                        reservation.getActivityName(), reservation.getReservationNo()));
                notification.setType(5);
                break;
        }

        if (!extraData.isEmpty()) {
            notification.setExtraData(JSON.toJSONString(extraData));
        }

        insertNotification(notification);
    }

    @Override
    public List<Notification> selectUnreadAuditNotifications(String userId) {
        return notificationMapper.selectUnreadAuditNotifications(userId);
    }
}
