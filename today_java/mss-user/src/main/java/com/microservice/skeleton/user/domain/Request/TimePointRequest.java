package com.microservice.skeleton.user.domain.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull; // javax -> jakarta
import java.time.LocalTime;

@Data
@Schema(description = "时间点信息请求对象")
public class TimePointRequest {

    @Schema(description = "时间点", requiredMode = Schema.RequiredMode.REQUIRED, example = "08:30:00")
    @NotNull(message = "时间点不能为空")
    private LocalTime point;

    @Schema(description = "状态：1-可用，0-不可用", example = "1")
    private Integer status = 1;
}
