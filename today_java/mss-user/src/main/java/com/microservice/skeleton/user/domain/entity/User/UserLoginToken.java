package com.microservice.skeleton.user.domain.entity.User;

import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@Data
public class UserLoginToken {

    // getter 和 setter 方法
    @NotNull
    private String token;

    public void setToken(String token) {
        this.token = token;
    }
}
