// UserProfileVO.java
package com.microservice.skeleton.user.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户个人信息视图对象")
public class UserProfileVO {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("微信昵称")
    private String nickname;

    @ApiModelProperty("微信头像")
    private String avatar;

    @ApiModelProperty("微信OpenID")
    private String openid;

    @ApiModelProperty("姓名")
    private String userName;

    @ApiModelProperty("学号")
    private String studentId;

    @ApiModelProperty("学院")
    private String college;

    @ApiModelProperty("专业")
    private String major;

    @ApiModelProperty("联系方式")
    private String phonenumber;
}
