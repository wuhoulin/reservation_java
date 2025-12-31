package com.microservice.skeleton.user.domain.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Min;      // javax -> jakarta
import jakarta.validation.constraints.NotBlank; // javax -> jakarta
import jakarta.validation.constraints.NotNull;   // javax -> jakarta
import jakarta.validation.constraints.Size;     // javax -> jakarta

@Data
@Schema(description = "教室信息请求对象")
public class RoomRequest {

    @Schema(description = "所属社区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所属社区ID不能为空")
    private Integer communityId;

    @Schema(description = "教室名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "党团活动室")
    @NotBlank(message = "教室名称不能为空")
    @Size(max = 50, message = "教室名称长度不能超过50个字符")
    private String name;

    @Schema(description = "容纳人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    @NotNull(message = "容纳人数不能为空")
    @Min(value = 1, message = "容纳人数必须大于0")
    private Integer capacity;

    @Schema(description = "教室图片URL", example = "https://example.com/images/room.jpg")
    private String imageUrl;

    @Schema(description = "教室描述", example = "适合举办党团活动和大型会议")
    private String description;

    @Schema(description = "状态：true-可用，false-不可用", example = "true")
    private Boolean status = true;
}
