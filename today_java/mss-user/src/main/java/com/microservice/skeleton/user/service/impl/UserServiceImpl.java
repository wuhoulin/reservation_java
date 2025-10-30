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
import com.microservice.skeleton.user.util.JwtUtil;
import com.microservice.skeleton.user.util.LogUtil;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Override
    public void loginByCAS(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uid = request.getRemoteUser();
        Principal principal = request.getUserPrincipal();
        logger.info("LoginByCAS 开始，remoteUser = {}, principal = {}", request.getRemoteUser(), request.getUserPrincipal());

        // 1. 判断 CAS 是否已认证
        if (!(principal instanceof AttributePrincipal)) {
            // CAS 登录失败
            response.sendRedirect("http://210.34.24.34:40080/#/errLogin?notice=10002");
            return;
        }
        logger.info("LoginByCAS 开始，remoteUser = {}, principal = {}", request.getRemoteUser(), request.getUserPrincipal());

        // 2. 从 CAS 拿属性
        AttributePrincipal aPrincipal = (AttributePrincipal) principal;
        Map<String, Object> attributes = aPrincipal.getAttributes();
        String cn = (String) attributes.get("cn");
        LogUtil.Debug("CAS Attributes: " + attributes);

        // 3. 查本地用户
        Example userExample = new Example(User.class);
        userExample.createCriteria().andEqualTo("account", uid);
        User user = userMapper.selectOneByExample(userExample);

        // 4. 如果不存在就自动注册
        if (user == null) {
            user = new User();
            user.setAccount(uid);
            user.setCn(cn);// 保存中文名
            // 可根据需要设置更多字段，比如 email、部门等：
            // user.setEmail((String)attributes.get("mail"));
            userMapper.insert(user);
        } else {
            // 可选：如果想保持 CAS 的最新中文名同步到本地
            user.setCn(cn);

        }
        // 5. 生成系统 Token 并重定向到前端
        Map<String, Object> tokenMap = getModelAndInitToken(user);
        String tokenName = (String) tokenMap.get("token");

        // 最后重定向到前端，并带上 token
        if (!response.isCommitted()) {
            response.sendRedirect("http://localhost:5173/#/?token=" + tokenName);

        }
    }
    private Map<String, Object> getModelAndInitToken(User user) {
        // 生成你系统自己的 token，比如 JWT
        String token = JwtUtil.createToken(user.getId(), user.getAccount());

        // 存储一些登录态信息，比如缓存 token 和用户的关联（可选）
        String userJson = JSON.toJSONString(user);
        redisTemplate.opsForValue().set("LOGIN_TOKEN:" + token, userJson, 30, TimeUnit.MINUTES);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public Object loginByToken(UserLoginToken userLoginVo) {
        // TODO Auto-generated method stub
        Token token = tokenManager.get(userLoginVo.getToken());
        if (null == token) {
            throw new BusinessException(BusinessError.ACCOUNT_ERROR, "该用户token过期，请重新登陆！");
        }
        User user = userMapper.selectById(token.getUserId());
        if (null == user) {
            tokenManager.deleteToken(token.getUserId(), userLoginVo.getToken());
            throw new BusinessException(BusinessError.ACCOUNT_ERROR, "该用户不存在，请联系系统管理员！");
        }
        return getModelAndInitToken(user);
    }
}
