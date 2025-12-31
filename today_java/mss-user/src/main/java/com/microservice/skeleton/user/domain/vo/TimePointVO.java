package com.microservice.skeleton.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "可用时间点视图对象")
public class TimePointVO {

    @Schema(description = "时间点ID", example = "1")
    private Integer id;

    @Schema(description = "时间点", example = "08:30:00")
    private LocalTime point;

    @Schema(description = "状态：1-可用，0-不可用", example = "1")
    private Integer status;

    @Schema(description = "是否可预约", example = "true")
    private Boolean available;
}
