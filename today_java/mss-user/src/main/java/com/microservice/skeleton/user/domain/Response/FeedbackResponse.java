package com.microservice.skeleton.user.domain.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "反馈响应")
public class FeedbackResponse {

    @Schema(description = "反馈ID")
    private Long id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "反馈标题")
    private String title;

    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "反馈类型")
    private Integer type;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "联系方式")
    private String contact;

    @Schema(description = "反馈图片")
    private List<String> images;

    @Schema(description = "状态：1-待处理，2-处理中，3-已处理，4-已关闭")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "管理员回复内容")
    private String replyContent;

    @Schema(description = "回复时间")
    private LocalDateTime repliedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;
}
