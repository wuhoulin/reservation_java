package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reservation_approvals")
@ApiModel(description = "预约审核信息")
public class ReservationApproval {

    @ApiModelProperty("审核ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("预约ID")
    @TableField("reservation_id")
    private Integer reservationId;

    @ApiModelProperty("审核管理员ID")
    @TableField("admin_id")
    private Integer adminId;

    @ApiModelProperty("审核状态：1-通过，2-拒绝")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("审核理由")
    @TableField("reason")
    private String reason;

    @ApiModelProperty("创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
