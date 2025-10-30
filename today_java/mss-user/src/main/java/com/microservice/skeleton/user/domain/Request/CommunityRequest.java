package com.microservice.skeleton.user.domain.Request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "社区信息请求对象")
public class CommunityRequest {

    @ApiModelProperty(value = "社区名称", required = true, example = "学生第一社区")
    @NotBlank(message = "社区名称不能为空")
    @Size(max = 50, message = "社区名称长度不能超过50个字符")
    private String name;

    @ApiModelProperty(value = "社区位置", required = true, example = "位于1号楼和2号楼")
    @NotBlank(message = "社区位置不能为空")
    @Size(max = 100, message = "社区位置长度不能超过100个字符")
    private String location;

    @ApiModelProperty(value = "社区描述", example = "学生第一社区包含多种功能教室，适合各类学生活动")
    private String description;
}
