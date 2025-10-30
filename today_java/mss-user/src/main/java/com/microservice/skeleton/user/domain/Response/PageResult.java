package com.microservice.skeleton.user.domain.Response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "分页结果对象")
public class PageResult<T> {

    @ApiModelProperty("记录列表")
    private List<T> records;

    @ApiModelProperty("总记录数")
    private Long total;

    @ApiModelProperty("当前页码")
    private Long current;

    @ApiModelProperty("每页数量")
    private Long size;
}
