package com.microservice.skeleton.user.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalTime;

@Data
@ApiModel(description = "可用时间点视图对象")
public class TimePointVO {

    @ApiModelProperty(value = "时间点ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "时间点", example = "08:30:00")
    private LocalTime point;

    @ApiModelProperty(value = "状态：1-可用，0-不可用", example = "1")
    private Integer status;

    @ApiModelProperty(value = "是否可预约", example = "true")
    private Boolean available;
}
