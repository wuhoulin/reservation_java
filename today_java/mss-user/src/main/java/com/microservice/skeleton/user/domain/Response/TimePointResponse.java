package com.microservice.skeleton.user.domain.Response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@ApiModel(description = "时间点信息响应对象")
public class TimePointResponse {

    @ApiModelProperty(value = "时间点ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "时间点", example = "08:30:00")
    private LocalTime point;

    @ApiModelProperty(value = "状态：1-可用，0-不可用", example = "1")
    private Integer status;

    @ApiModelProperty(value = "创建时间", example = "2023-05-01T10:00:00")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间", example = "2023-05-01T10:00:00")
    private LocalDateTime updatedAt;
}
