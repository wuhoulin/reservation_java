package com.microservice.skeleton.user.domain.Request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "教室信息请求对象")
public class RoomRequest {

    @ApiModelProperty(value = "所属社区ID", required = true, example = "1")
    @NotNull(message = "所属社区ID不能为空")
    private Integer communityId;

    @ApiModelProperty(value = "教室名称", required = true, example = "党团活动室")
    @NotBlank(message = "教室名称不能为空")
    @Size(max = 50, message = "教室名称长度不能超过50个字符")
    private String name;

    @ApiModelProperty(value = "容纳人数", required = true, example = "80")
    @NotNull(message = "容纳人数不能为空")
    @Min(value = 1, message = "容纳人数必须大于0")
    private Integer capacity;

    @ApiModelProperty(value = "教室图片URL", example = "https://example.com/images/room.jpg")
    private String imageUrl;

    @ApiModelProperty(value = "教室描述", example = "适合举办党团活动和大型会议")
    private String description;

    @ApiModelProperty(value = "状态：true-可用，false-不可用", example = "true")
    private Boolean status = true;
}
