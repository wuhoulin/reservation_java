package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("time_points")  // 修改表名为 time_points
@ApiModel(description = "时间点信息")
public class TimePoint {

    @ApiModelProperty("时间点ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("时间点（格式：HH:mm:ss）")
    @TableField("point")
    private LocalTime point;  // 替换原来的 startTime 和 endTime

    @ApiModelProperty("状态：1-可用，0-不可用")
    @TableField("status")
    private Integer status;  // 改为 Integer 类型，与数据库一致

    @ApiModelProperty("创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 移除了 description 字段，因为 time_points 表中没有该字段
}
