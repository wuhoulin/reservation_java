package com.microservice.skeleton.user.util;

import org.springframework.validation.BindingResult;

public class CommonUtil {
    public static String processErrorString(BindingResult bindingResult) {
        StringBuilder errorMessage = new StringBuilder();
        bindingResult.getAllErrors().forEach(error -> {
            errorMessage.append(error.getDefaultMessage()).append(" ");
        });
        return errorMessage.toString();
    }
}
