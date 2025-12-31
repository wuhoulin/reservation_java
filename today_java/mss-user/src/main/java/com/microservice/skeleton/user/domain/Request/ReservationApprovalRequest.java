package com.microservice.skeleton.user.domain.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull; // javax -> jakarta
import jakarta.validation.constraints.Size;    // javax -> jakarta

@Data
@Schema(description = "预约审核请求对象")
public class ReservationApprovalRequest {

    @Schema(description = "预约ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "预约ID不能为空")
    private Integer reservationId;

    @Schema(description = "审核管理员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "审核管理员ID不能为空")
    private Integer adminId;

    @Schema(description = "审核状态：1-通过，2-拒绝", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    @Schema(description = "审核理由", example = "符合预约条件，予以通过")
    @Size(max = 255, message = "审核理由长度不能超过255个字符")
    private String reason;
}
