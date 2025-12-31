package com.microservice.skeleton.user.domain.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "API通用响应对象")
public class ApiResponse<T> {

    @Schema(description = "状态码：200-成功，400-参数错误，500-系统错误", example = "200") // ✅ 替换了 @ApiModelProperty
    private Integer code;

    @Schema(description = "消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "字段级错误详情（如参数校验失败）")
    private Map<String, String> errors;


    // 成功响应（无数据）
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    // 成功响应（带数据）
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
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
        response.setCode(400);
        response.setMessage("参数校验失败");
        response.setErrors(errors);
        return response;
    }

    // 未授权错误
    public static <T> ApiResponse<T> unauthorized(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    // 未找到资源错误
    public static <T> ApiResponse<T> notFound(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(404);
        response.setMessage(message);
        return response;
    }

    // 重载方法，支持自定义状态码
    public static <T> ApiResponse<T> notFound(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
