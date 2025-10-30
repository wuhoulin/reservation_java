package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rooms")
@ApiModel(description = "教室信息")
public class Room {

    @ApiModelProperty("教室ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("所属社区ID")
    @TableField("community_id")
    private Integer communityId;

    @ApiModelProperty("教室名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("容纳人数")
    @TableField("capacity")
    private Integer capacity;

    @ApiModelProperty("教室图片URL")
    @TableField("image_url")
    private String imageUrl;

    @ApiModelProperty("教室描述")
    @TableField("description")
    private String description;

    @ApiModelProperty("状态：true-可用，false-不可用")
    @TableField("status")
    private Boolean status;

    @ApiModelProperty("创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
