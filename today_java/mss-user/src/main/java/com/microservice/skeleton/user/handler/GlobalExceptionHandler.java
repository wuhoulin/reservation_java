package com.microservice.skeleton.user.handler;

import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.Response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理 @Valid 参数校验失败
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 强制返回 HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ApiResponse.validationError(errors); // 返回字段级错误
    }

    // 处理其他业务异常
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Object> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(4001, ex.getMessage()); // 自定义业务错误码
    }
}
