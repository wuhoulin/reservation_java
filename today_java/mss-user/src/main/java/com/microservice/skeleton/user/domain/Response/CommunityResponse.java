package com.microservice.skeleton.user.domain.Response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "社区信息响应对象")
public class CommunityResponse {

    @ApiModelProperty(value = "社区ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "社区名称", example = "学生第一社区")
    private String name;

    @ApiModelProperty(value = "社区位置", example = "位于1号楼和2号楼")
    private String location;

    @ApiModelProperty(value = "社区描述", example = "学生第一社区包含多种功能教室，适合各类学生活动")
    private String description;

    @ApiModelProperty(value = "创建时间", example = "2023-05-01T10:00:00")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间", example = "2023-05-01T10:00:00")
    private LocalDateTime updatedAt;
}
