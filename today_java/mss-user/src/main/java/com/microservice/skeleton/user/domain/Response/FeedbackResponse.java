package com.microservice.skeleton.user.domain.Response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("反馈响应")
public class FeedbackResponse {

    @ApiModelProperty("反馈ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("反馈标题")
    private String title;

    @ApiModelProperty("反馈内容")
    private String content;

    @ApiModelProperty("反馈类型")
    private Integer type;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("联系方式")
    private String contact;

    @ApiModelProperty("反馈图片")
    private List<String> images;

    @ApiModelProperty("状态：1-待处理，2-处理中，3-已处理，4-已关闭")
    private Integer status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("管理员回复内容")
    private String replyContent;

    @ApiModelProperty("回复时间")
    private LocalDateTime repliedAt;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("用户头像")
    private String userAvatar;
}
