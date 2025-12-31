package com.microservice.skeleton.user.domain.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "收藏响应")
public class RoomFavoriteResponse {

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "教室ID")
    private Integer roomId;

    @Schema(description = "教室名称")
    private String roomName;

    @Schema(description = "容纳人数")
    private Integer capacity;

    @Schema(description = "教室图片")
    private String imageUrl;

    @Schema(description = "所属社区ID")
    private Integer communityId;

    @Schema(description = "收藏时间")
    private LocalDateTime createdAt;

    @Schema(description = "是否收藏")
    private Boolean isFavorited;
}
