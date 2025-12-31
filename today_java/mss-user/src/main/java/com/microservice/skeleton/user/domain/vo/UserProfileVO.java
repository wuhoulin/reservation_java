package com.microservice.skeleton.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户个人信息视图对象")
public class UserProfileVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "微信昵称")
    private String nickname;

    @Schema(description = "微信头像")
    private String avatar;

    @Schema(description = "微信OpenID")
    private String openid;

    @Schema(description = "姓名")
    private String userName;

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "联系方式")
    private String phonenumber;

    @Schema(description = "邮箱")
    private String email;
}
