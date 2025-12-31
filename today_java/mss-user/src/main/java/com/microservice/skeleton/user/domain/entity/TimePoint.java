package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema; // 修改
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("time_points")
@Schema(description = "时间点信息") // 替换 @ApiModel
public class TimePoint {

    @Schema(description = "时间点ID") // 替换 @ApiModelProperty
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "时间点（格式：HH:mm:ss）")
    @TableField("point")
    private LocalTime point;

    @Schema(description = "状态：1-可用，0-不可用")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
