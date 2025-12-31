package com.microservice.skeleton.user.domain.Response;

import io.swagger.v3.oas.annotations.media.Schema; // 修改：使用 OpenAPI 3 注解
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "社区信息响应对象") // 替换 @ApiModel
public class CommunityResponse {

    @Schema(description = "社区ID", example = "1") // 替换 @ApiModelProperty
    private Integer id;

    @Schema(description = "社区名称", example = "学生第一社区")
    private String name;

    @Schema(description = "社区位置", example = "位于1号楼和2号楼")
    private String location;

    @Schema(description = "社区描述", example = "学生第一社区包含多种功能教室，适合各类学生活动")
    private String description;

    @Schema(description = "创建时间", example = "2023-05-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2023-05-01T10:00:00")
    private LocalDateTime updatedAt;
}
