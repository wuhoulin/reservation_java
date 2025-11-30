package com.microservice.skeleton.user.domain.Request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("收藏请求")
public class RoomFavoriteRequest {

    @ApiModelProperty(value = "教室ID", required = true)
    @NotNull(message = "教室ID不能为空")
    private Integer roomId;
}
