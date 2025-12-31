package com.microservice.skeleton.user.domain.Request;

import io.swagger.v3.oas.annotations.media.Schema; // 修改：Swagger 2 -> OpenAPI 3
import lombok.Data;

import jakarta.validation.constraints.NotBlank; // 修改：javax -> jakarta
import jakarta.validation.constraints.Size;     // 修改：javax -> jakarta

@Data
@Schema(description = "社区信息请求对象") // 替换 @ApiModel
public class CommunityRequest {

    @Schema(description = "社区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "学生第一社区") // 替换 @ApiModelProperty
    @NotBlank(message = "社区名称不能为空")
    @Size(max = 50, message = "社区名称长度不能超过50个字符")
    private String name;

    @Schema(description = "社区位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "位于1号楼和2号楼")
    @NotBlank(message = "社区位置不能为空")
    @Size(max = 100, message = "社区位置长度不能超过100个字符")
    private String location;

    @Schema(description = "社区描述", example = "学生第一社区包含多种功能教室，适合各类学生活动")
    private String description;
}
