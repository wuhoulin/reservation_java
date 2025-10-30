package com.microservice.skeleton.user.error;

public class BusinessError {
    // 错误代码
    public static final Integer PARAMETER_VALIDATION_ERROR = 10001;
    public static final Integer STUDENT_INFO_NO = 10002;
    public static final Integer TOKEN_INVALID = 10003;
    public static final Integer USER_NOT_FOUND = 10004;
    public static final Integer ACCOUNT_ERROR = 10005; // ✅ 添加账号相关错误码

    // 错误描述
    public static final String PARAMETER_VALIDATION_ERROR_MSG = "参数验证错误";
    public static final String STUDENT_INFO_NO_MSG = "学生信息未找到";
    public static final String TOKEN_INVALID_MSG = "Token 无效或已过期";
    public static final String USER_NOT_FOUND_MSG = "用户未找到";
    public static final String ACCOUNT_ERROR_MSG = "账号错误或状态异常"; // ✅ 添加账号错误描述
}
