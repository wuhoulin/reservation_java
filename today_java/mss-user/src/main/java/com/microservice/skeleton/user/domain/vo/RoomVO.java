package com.microservice.skeleton.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "教室视图对象")
public class RoomVO {

    @Schema(description = "教室ID", example = "1")
    private Integer id;

    @Schema(description = "教室名称", example = "党团活动室")
    private String name;

    @Schema(description = "所属社区ID", example = "1")
    private Integer communityId;

    @Schema(description = "所属社区名称", example = "学生第一社区")
    private String communityName;

    @Schema(description = "容纳人数", example = "80")
    private Integer capacity;

    @Schema(description = "教室图片URL", example = "https://example.com/images/room.jpg")
    private String imageUrl;

    @Schema(description = "教室描述", example = "适合举办党团活动和大型会议")
    private String description;

    @Schema(description = "状态：true-可用，false-不可用", example = "true")
    private Boolean status;

    @Schema(description = "是否可预约", example = "true")
    private Boolean available;
}
