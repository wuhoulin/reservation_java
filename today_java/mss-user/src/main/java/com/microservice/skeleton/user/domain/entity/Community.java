package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("communities")
public class Community {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("location")
    private String location;


    @TableField("description")
    private String description;


    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;


    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
