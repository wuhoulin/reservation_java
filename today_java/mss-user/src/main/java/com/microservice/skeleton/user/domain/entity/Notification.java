// Notification.java
package com.microservice.skeleton.user.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 通知对象 notifications
 */
@Data
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 通知ID */
    private Long id;

    /** 接收用户ID */
    private String userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型：1-预约成功，2-预约审核通过，3-预约审核拒绝，4-预约取消，5-预约完成，6-系统通知 */
    private Integer type;

    /** 关联类型：1-预约，2-教室，3-系统 */
    private Integer relatedType;

    /** 关联ID */
    private String relatedId;

    /** 预约编号 */
    private String reservationNo;

    /** 教室ID */
    private Long roomId;

    /** 是否已读：0-未读，1-已读 */
    private Integer isRead;

    /** 阅读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;

    /** 额外数据 */
    private String extraData;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    // getters and setters
    // toString()
}
