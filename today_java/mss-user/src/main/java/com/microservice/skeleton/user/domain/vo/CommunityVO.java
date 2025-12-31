package com.microservice.skeleton.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "社区视图对象")
public class CommunityVO {

    @Schema(description = "社区ID", example = "1")
    private Integer id;

    @Schema(description = "社区名称", example = "学生第一社区")
    private String name;

    @Schema(description = "社区位置", example = "位于1号楼和2号楼")
    private String location;

    @Schema(description = "社区描述", example = "学生第一社区包含多种功能教室，适合各类学生活动")
    private String description;

    @Schema(description = "教室列表")
    private List<RoomVO> rooms;
}
