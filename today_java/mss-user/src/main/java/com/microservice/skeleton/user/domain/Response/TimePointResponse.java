package com.microservice.skeleton.user.domain.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "时间点信息响应对象")
public class TimePointResponse {

    @Schema(description = "时间点ID", example = "1")
    private Integer id;

    @Schema(description = "时间点", example = "08:30:00")
    private LocalTime point;

    @Schema(description = "状态：1-可用，0-不可用", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2023-05-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2023-05-01T10:00:00")
    private LocalDateTime updatedAt;
}
