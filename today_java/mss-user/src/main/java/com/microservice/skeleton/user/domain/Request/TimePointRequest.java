package com.microservice.skeleton.user.domain.Request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Data
@ApiModel(description = "时间点信息请求对象")
public class TimePointRequest {

    @ApiModelProperty(value = "时间点", required = true, example = "08:30:00")
    @NotNull(message = "时间点不能为空")
    private LocalTime point;

    @ApiModelProperty(value = "状态：1-可用，0-不可用", example = "1")
    private Integer status = 1;
}
