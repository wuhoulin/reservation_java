package com.microservice.skeleton.user.domain.Response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel("收藏响应")
public class RoomFavoriteResponse {

    @ApiModelProperty("收藏ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("教室ID")
    private Integer roomId;

    @ApiModelProperty("教室名称")
    private String roomName;

    @ApiModelProperty("容纳人数")
    private Integer capacity;

    @ApiModelProperty("教室图片")
    private String imageUrl;

    @ApiModelProperty("所属社区ID")
    private Integer communityId;

    @ApiModelProperty("收藏时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("是否收藏")
    private Boolean isFavorited;
}
