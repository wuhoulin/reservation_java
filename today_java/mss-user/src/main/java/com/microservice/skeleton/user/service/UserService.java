package com.microservice.skeleton.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.entity.User.User;
import com.microservice.skeleton.user.domain.entity.User.UserLoginToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService extends IService<User> {


    void loginByCAS(HttpServletRequest request, HttpServletResponse response) throws Exception;



    Object loginByToken(UserLoginToken userLoginVo);

}
