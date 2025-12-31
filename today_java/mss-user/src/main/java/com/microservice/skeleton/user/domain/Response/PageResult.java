package com.microservice.skeleton.user.domain.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "分页结果对象")
public class PageResult<T> {

    @Schema(description = "记录列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "每页数量")
    private Long size;
}
