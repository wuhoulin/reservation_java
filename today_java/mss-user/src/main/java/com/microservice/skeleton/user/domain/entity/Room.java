package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema; // 修改
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rooms")
@Schema(description = "教室信息")
public class Room {

    @Schema(description = "教室ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "所属社区ID")
    @TableField("community_id")
    private Integer communityId;

    @Schema(description = "教室名称")
    @TableField("name")
    private String name;

    @Schema(description = "容纳人数")
    @TableField("capacity")
    private Integer capacity;

    @Schema(description = "教室图片URL")
    @TableField("image_url")
    private String imageUrl;

    @Schema(description = "教室描述")
    @TableField("description")
    private String description;

    @Schema(description = "状态：true-可用，false-不可用")
    @TableField("status")
    private Boolean status;

    @Schema(description = "创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;
}
