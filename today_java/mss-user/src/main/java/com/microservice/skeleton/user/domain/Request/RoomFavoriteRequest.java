package com.microservice.skeleton.user.domain.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull; // javax -> jakarta

@Data
@Schema(description = "收藏请求")
public class RoomFavoriteRequest {

    @Schema(description = "教室ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "教室ID不能为空")
    private Integer roomId;
}
