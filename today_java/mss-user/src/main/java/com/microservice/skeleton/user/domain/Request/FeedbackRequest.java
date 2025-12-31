package com.microservice.skeleton.user.domain.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank; // javax -> jakarta
import jakarta.validation.constraints.NotNull;   // javax -> jakarta
import java.util.List;

@Data
@Schema(description = "反馈请求")
public class FeedbackRequest {

    @Schema(description = "反馈标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "反馈标题不能为空")
    private String title;

    @Schema(description = "反馈内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "反馈内容不能为空")
    private String content;

    @Schema(description = "反馈类型：1-功能建议，2-界面问题，3-性能问题，4-内容错误，5-其他", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "反馈类型不能为空")
    private Integer type;

    @Schema(description = "联系方式")
    private String contact;

    @Schema(description = "反馈图片")
    private List<String> images;
}
