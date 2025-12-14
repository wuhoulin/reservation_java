package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 教室日期-时间点配置表
 */
@Data
@TableName("room_reserve_date")
public class RoomReserveDate {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 教室ID（关联 rooms.id） */
    private Integer roomId;

    /** 预约日期（格式：YYYY-MM-DD） */
    private LocalDate reserveDate;

    /** 时间点ID（关联 time_points.id） */
    private Integer timePointId;

    /** 状态：1-可用，0-不可用/已被预约 */
    private Integer status = 1;

    /** 关联的预约ID（新增字段） */
    private Long reservationId;

    /** 关联的预约编号（新增字段） */
    private String reservationNo;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
