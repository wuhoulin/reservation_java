package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("room_time") // 对应表名
public class RoomTime {
    @TableId(type = IdType.AUTO)
    private Integer id; // 主键ID
    private Integer roomId; // 教室ID
    private String timePoint; // 时间点（HH:MM）
    private Integer status; // 1-可用，0-不可用
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
