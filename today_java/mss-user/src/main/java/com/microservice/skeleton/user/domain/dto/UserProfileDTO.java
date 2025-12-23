// UserProfileDTO.java
package com.microservice.skeleton.user.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "用户个人信息DTO")
public class UserProfileDTO {

    @ApiModelProperty(value = "姓名")
    @Length(max = 30, message = "姓名长度不能超过30个字符")
    private String userName;

    @ApiModelProperty(value = "学号")
    @Length(max = 50, message = "学号长度不能超过50个字符")
    private String studentId;

    @ApiModelProperty(value = "学院")
    @Length(max = 100, message = "学院长度不能超过100个字符")
    private String college;

    @ApiModelProperty(value = "专业")
    @Length(max = 100, message = "专业长度不能超过100个字符")
    private String major;

    @ApiModelProperty(value = "联系方式")
    @Pattern(regexp = "^(|1[3-9]\\d{9})$", message = "手机号格式不正确")
    @Length(max = 11, message = "手机号长度不能超过11个字符")
    private String phonenumber;

    @ApiModelProperty(value = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Length(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;
}
