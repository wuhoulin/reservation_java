package com.microservice.skeleton.user.domain.entity.User;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.sql.Timestamp;

@Data
@ApiModel("系统用户")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user") // 指定数据库表名
public class User {

    @ApiModelProperty("用户ID（主键，自增）")
    @TableId("id")
    private Long id;

    @ApiModelProperty("用户账号（CAS 登录唯一标识）")
    @TableField("account")
    private String account;

    @ApiModelProperty("用户姓名")
    @TableField("name")
    private String name;

    @ApiModelProperty("用户邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty("用户电话")
    @TableField("phone")
    private String phone;

    @ApiModelProperty("用户状态（1：启用，0：禁用）")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private Timestamp createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private Timestamp updateTime;
    @ApiModelProperty("用户密码（如支持本地登录）")
    @TableField("password")
    private String password;
    private String cn; // 中文名字段
}
