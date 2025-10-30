package com.microservice.skeleton.user.res;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class CommonRes {

    // getter 和 setter 方法
    private int code;
    private String message;
    private Object data;

    // 构造函数
    public CommonRes(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 工厂方法创建 CommonRes 实例
    public static CommonRes create(Object data) {
        return new CommonRes(200, "Success", data);
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
