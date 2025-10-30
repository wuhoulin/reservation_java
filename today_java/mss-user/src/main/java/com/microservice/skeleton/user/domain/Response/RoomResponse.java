package com.microservice.skeleton.user.domain.Response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "教室信息响应对象")
public class RoomResponse {

    @ApiModelProperty(value = "教室ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "所属社区ID", example = "1")
    private Integer communityId;

    @ApiModelProperty(value = "所属社区名称", example = "学生第一社区")
    private String communityName;

    @ApiModelProperty(value = "教室名称", example = "党团活动室")
    private String name;

    @ApiModelProperty(value = "容纳人数", example = "80")
    private Integer capacity;

    @ApiModelProperty(value = "教室图片URL", example = "https://example.com/images/room.jpg")
    private String imageUrl;

    @ApiModelProperty(value = "教室描述", example = "适合举办党团活动和大型会议")
    private String description;

    @ApiModelProperty(value = "状态：true-可用，false-不可用", example = "true")
    private Boolean status;

    @ApiModelProperty(value = "创建时间", example = "2023-05-01T10:00:00")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间", example = "2023-05-01T10:00:00")
    private LocalDateTime updatedAt;
}
