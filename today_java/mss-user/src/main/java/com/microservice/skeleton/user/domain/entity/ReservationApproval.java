package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema; // 修改
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reservation_approvals")
@Schema(description = "预约审核信息")
public class ReservationApproval {

    @Schema(description = "审核ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "预约ID")
    @TableField("reservation_id")
    private Integer reservationId;

    @Schema(description = "审核管理员ID")
    @TableField("admin_id")
    private Integer adminId;

    @Schema(description = "审核状态：1-通过，2-拒绝")
    @TableField("status")
    private Integer status;

    @Schema(description = "审核理由")
    @TableField("reason")
    private String reason;

    @Schema(description = "创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
