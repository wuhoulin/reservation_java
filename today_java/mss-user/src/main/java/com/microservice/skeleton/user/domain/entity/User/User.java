package com.microservice.skeleton.user.domain.entity.User;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema; // 修改：Swagger 2 -> OpenAPI 3
import lombok.*;

import java.time.LocalDateTime;

@Data
@Schema(description = "系统用户") // 替换 @ApiModel
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class User {

    @Schema(description = "用户ID（主键，自增）") // 替换 @ApiModelProperty
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @Schema(description = "部门ID")
    @TableField("dept_id")
    private Long deptId;

    @Schema(description = "登录账号")
    @TableField("login_name")
    private String loginName;

    @Schema(description = "用户账号")
    @TableField("user_name")
    private String userName;

    @Schema(description = "用户昵称")
    @TableField("nick_name")
    private String nickName;

    @Schema(description = "微信openid")
    @TableField("openid")
    private String openid;

    @Schema(description = "用户类型（00系统用户 01微信用户）")
    @TableField("user_type")
    private String userType;

    @Schema(description = "用户邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "手机号码")
    @TableField("phonenumber")
    private String phonenumber;

    @Schema(description = "学号")
    @TableField("student_id")
    private String studentId;

    @Schema(description = "学院")
    @TableField("college")
    private String college;

    @Schema(description = "专业")
    @TableField("major")
    private String major;

    @Schema(description = "用户性别（0男 1女 2未知）")
    @TableField("sex")
    private String sex;

    @Schema(description = "头像地址")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "密码")
    @TableField("password")
    private String password;

    @Schema(description = "帐号状态（0正常 1停用）")
    @TableField("status")
    private String status;

    @Schema(description = "删除标志（0代表存在 2代表删除）")
    @TableField("del_flag")
    private String delFlag;

    @Schema(description = "最后登录IP")
    @TableField("login_ip")
    private String loginIp;

    @Schema(description = "最后登录时间")
    @TableField("login_date")
    private LocalDateTime loginDate;

    @Schema(description = "创建者")
    @TableField("create_by")
    private String createBy;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    @TableField("update_by")
    private String updateBy;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
