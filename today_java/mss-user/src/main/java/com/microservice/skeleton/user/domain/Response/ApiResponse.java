package com.microservice.skeleton.user.domain.Response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel(description = "API响应对象")
public class ApiResponse<T> {

    @ApiModelProperty(value = "状态码：200-成功，400-参数错误，500-系统错误", example = "200")
    private Integer code;

    @ApiModelProperty(value = "消息", example = "操作成功")
    private String message;

    @ApiModelProperty("响应数据")
    private T data;

    @ApiModelProperty("字段级错误详情（如参数校验失败）")
    private Map<String, String> errors;

    // 成功响应（无数据）
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    // 成功响应（带数据）
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200); // HTTP 标准成功码
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    // 错误响应（通用）
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    // 参数校验错误（带字段详情）
    public static <T> ApiResponse<T> validationError(Map<String, String> errors) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(400); // HTTP 400 Bad Request
        response.setMessage("参数校验失败");
        response.setErrors(errors);
        return response;
    }
}
