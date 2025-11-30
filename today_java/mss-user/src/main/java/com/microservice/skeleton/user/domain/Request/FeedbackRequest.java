package com.microservice.skeleton.user.domain.Request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("反馈请求")
public class FeedbackRequest {

    @ApiModelProperty(value = "反馈标题", required = true)
    @NotBlank(message = "反馈标题不能为空")
    private String title;

    @ApiModelProperty(value = "反馈内容", required = true)
    @NotBlank(message = "反馈内容不能为空")
    private String content;

    @ApiModelProperty(value = "反馈类型：1-功能建议，2-界面问题，3-性能问题，4-内容错误，5-其他", required = true)
    @NotNull(message = "反馈类型不能为空")
    private Integer type;

    @ApiModelProperty("联系方式")
    private String contact;

    @ApiModelProperty("反馈图片")
    private List<String> images;
}
