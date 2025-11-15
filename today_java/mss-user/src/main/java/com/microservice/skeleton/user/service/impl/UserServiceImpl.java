package com.microservice.skeleton.user.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.microservice.skeleton.user.Manager.TokenManager;
import com.microservice.skeleton.user.domain.entity.Token;
import com.microservice.skeleton.user.error.BusinessError;
import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.entity.User.User;
import com.microservice.skeleton.user.domain.entity.User.UserLoginToken;
import com.microservice.skeleton.user.mapper.UserMapper;
import com.microservice.skeleton.user.service.UserService;
import com.microservice.skeleton.user.util.LogUtil;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import tk.mybatis.mapper.entity.Example;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private TokenManager tokenManager;


}
