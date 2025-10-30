package com.microservice.skeleton.user.domain.Request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "预约审核请求对象")
public class ReservationApprovalRequest {

    @ApiModelProperty(value = "预约ID", required = true, example = "1")
    @NotNull(message = "预约ID不能为空")
    private Integer reservationId;

    @ApiModelProperty(value = "审核管理员ID", required = true, example = "1")
    @NotNull(message = "审核管理员ID不能为空")
    private Integer adminId;

    @ApiModelProperty(value = "审核状态：1-通过，2-拒绝", required = true, example = "1")
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "审核理由", example = "符合预约条件，予以通过")
    @Size(max = 255, message = "审核理由长度不能超过255个字符")
    private String reason;
}
