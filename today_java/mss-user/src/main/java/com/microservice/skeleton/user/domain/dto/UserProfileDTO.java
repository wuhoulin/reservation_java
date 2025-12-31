package com.microservice.skeleton.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema; // 修改：Swagger 2 -> OpenAPI 3
import lombok.Data;


import jakarta.validation.constraints.Email;   // 修改：javax -> jakarta
import jakarta.validation.constraints.Pattern; // 修改：javax -> jakarta
import org.checkerframework.checker.units.qual.Length;

@Data
@Schema(description = "用户个人信息DTO") // 替换 @ApiModel
public class UserProfileDTO {

    @Schema(description = "姓名") // 替换 @ApiModelProperty

    private String userName;

    @Schema(description = "学号")

    private String studentId;

    @Schema(description = "学院")

    private String college;

    @Schema(description = "专业")

    private String major;

    @Schema(description = "联系方式")
    @Pattern(regexp = "^(|1[3-9]\\d{9})$", message = "手机号格式不正确")

    private String phonenumber;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")

    private String email;
}
