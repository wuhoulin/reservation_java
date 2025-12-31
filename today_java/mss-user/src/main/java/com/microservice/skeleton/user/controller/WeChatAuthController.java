package com.microservice.skeleton.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.skeleton.user.domain.entity.User.User;
import com.microservice.skeleton.user.mapper.UserMapper;
import com.microservice.skeleton.user.service.UserService;
import com.microservice.skeleton.user.service.UserWechatService;
import com.microservice.skeleton.user.util.JwtTokenUtil;
import com.microservice.skeleton.user.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wechat")
@Slf4j
public class WeChatAuthController {

    @Value("${wechat.appId}")
    private String appId;

    @Value("${wechat.appSecret}")
    private String appSecret;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserWechatService userWechatService;

    // 注入UserMapper，用于直接操作数据库
    @Autowired
    private UserMapper userMapper;

    /**
     * 生成微信授权URL
     */
    /**
     * 生成微信授权URL - HTTPS版本
     */
    @GetMapping("/generate-auth-url")
    public Map<String, String> generateAuthUrl(
            @RequestParam String redirectPath,
            @RequestParam(defaultValue = "snsapi_userinfo") String scope) {

        try {
            // ⚠️ 关键修复：改为HTTPS
            String redirectUri = "https://ndnu-yuyue.xyz" + redirectPath;

            log.info("=== 微信授权详细调试（HTTPS）===");
            log.info("AppId: {}", appId);
            log.info("授权范围: {}", scope);
            log.info("前端路径: {}", redirectPath);
            log.info("生成的redirect_uri (HTTPS): {}", redirectUri);

            String encodedRedirectUri = URLEncoder.encode(redirectUri, "UTF-8");
            log.info("编码后的redirect_uri: {}", encodedRedirectUri);

            String state = generateRandomState();
            log.info("生成的state: {}", state);

            String authUrl = String.format(
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s#wechat_redirect",
                    appId, encodedRedirectUri, scope, state);

            log.info("完整的授权URL: {}", authUrl);
            log.info("=========================");

            Map<String, String> result = new HashMap<>();
            result.put("authUrl", authUrl);
            result.put("state", state);
            result.put("scope", scope);
            result.put("debug_redirectUri", redirectUri);
            result.put("debug_encodedRedirectUri", encodedRedirectUri);

            return result;

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL编码失败", e);
        }
    }

    /**
     * 使用code换取openid和用户信息
     */
    @PostMapping("/exchange-code")
    public Map<String, Object> exchangeCode(@RequestBody CodeRequest request, HttpServletRequest httpRequest) {
        String code = request.getCode();
        String state = request.getState();

        if (code == null || code.trim().isEmpty()) {
            log.error("错误: code参数为空或为空字符串");
            throw new RuntimeException("code参数不能为空");
        }

        // 第一步：通过code获取access_token和openid
        String tokenUrl = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        log.info("请求微信token URL: {}", tokenUrl);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String tokenResponse = restTemplate.getForObject(tokenUrl, String.class);

            log.info("微信token响应: {}", tokenResponse);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> tokenData = mapper.readValue(tokenResponse, new TypeReference<Map<String, Object>>() {});

            if (tokenData.containsKey("openid")) {
                String openid = (String) tokenData.get("openid");
                String accessToken = (String) tokenData.get("access_token");
                String refreshToken = (String) tokenData.get("refresh_token");
                Integer expiresIn = (Integer) tokenData.get("expires_in");
                String scope = (String) tokenData.get("scope");

                log.info("成功获取openid: {}", openid);
                log.info("access_token: {}", accessToken);
                log.info("scope: {}", scope);

                WeChatUserInfo userInfo = new WeChatUserInfo();
                userInfo.setOpenid(openid);
                userInfo.setAccessToken(accessToken);
                userInfo.setRefreshToken(refreshToken);
                userInfo.setExpiresIn(expiresIn);
                userInfo.setScope(scope);
                userInfo.setAuthTime(System.currentTimeMillis());

                User systemUser = null;

                // 第二步：如果scope包含snsapi_userinfo，则获取用户详细信息
                if ("snsapi_userinfo".equals(scope)) {
                    try {
                        String userInfoUrl = String.format(
                                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                                accessToken, openid);

                        log.info("请求用户信息URL: {}", userInfoUrl);

                        String userInfoResponse = restTemplate.getForObject(userInfoUrl, String.class);
                        log.info("用户信息响应: {}", userInfoResponse);

                        Map<String, Object> userInfoData = mapper.readValue(userInfoResponse,
                                new TypeReference<Map<String, Object>>() {});

                        if (userInfoData.containsKey("nickname")) {
                            // 成功获取用户信息
                            userInfo.setNickname((String) userInfoData.get("nickname"));
                            userInfo.setSex((Integer) userInfoData.get("sex"));
                            userInfo.setProvince((String) userInfoData.get("province"));
                            userInfo.setCity((String) userInfoData.get("city"));
                            userInfo.setCountry((String) userInfoData.get("country"));
                            userInfo.setUnionid((String) userInfoData.get("unionid"));

                            // 🟢 获取头像逻辑 (新增)
                            if (userInfoData.containsKey("headimgurl")) {
                                String headImgUrl = (String) userInfoData.get("headimgurl");
                                // 微信返回的通常是http，建议转为https防止浏览器混合内容警告
                                if (headImgUrl != null && headImgUrl.startsWith("http:")) {
                                    headImgUrl = headImgUrl.replace("http:", "https:");
                                }
                                userInfo.setHeadimgurl(headImgUrl);
                            }

                            log.info("成功获取用户详细信息:");
                            log.info("昵称: {}", userInfo.getNickname());
                            log.info("头像: {}", userInfo.getHeadimgurl());

                            // 关键：创建/更新用户，包含头像
                            systemUser = createOrUpdateUserWithOpenid(userInfo);

                            log.info("微信用户授权成功: userId={}, openid={}, nickname={}",
                                    systemUser.getUserId(), openid, userInfo.getNickname());
                        } else {
                            log.error("获取用户详细信息失败: {}", userInfoData.get("errmsg"));
                            // 即使没有详细信息，也创建基础用户（带openid）
                            systemUser = createOrUpdateUserWithOpenid(userInfo);
                        }
                    } catch (Exception e) {
                        log.error("获取用户详细信息异常: {}", e.getMessage(), e);
                        // 异常情况下也创建基础用户（带openid）
                        systemUser = createOrUpdateUserWithOpenid(userInfo);
                    }
                } else {
                    log.info("当前授权范围: {}，无法获取用户详细信息", scope);
                    // 静默授权情况下创建基础用户（带openid）
                    systemUser = createOrUpdateUserWithOpenid(userInfo);
                }

                // 生成JWT token
                Map<String, Object> claims = new HashMap<>();
                claims.put("openid", openid);
                claims.put("authTime", userInfo.getAuthTime());
                claims.put("scope", scope);
                claims.put("nickname", userInfo.getNickname());

                if (systemUser != null) {
                    claims.put("userId", systemUser.getUserId());
                    claims.put("userName", systemUser.getUserName());
                    claims.put("openid", systemUser.getOpenid()); // JWT中携带用户表的openid
                    // 可以选择将头像也放入token，但token会变长，建议前端从userInfo读取
                    // claims.put("avatar", systemUser.getAvatar());
                }

                String jwtToken = jwtTokenUtil.generateToken(openid, claims);
                log.info("生成的JWT Token: {}", jwtToken);

                // 将用户信息存入Session
                httpRequest.getSession().setAttribute("currentUser", userInfo);
                log.info("用户信息已存入Session: {}", userInfo);

                // 设置到ThreadLocal
                UserContext.setCurrentUser(userInfo);

                // 返回统一格式的响应
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "授权成功");
                result.put("openid", openid);
                result.put("accessToken", accessToken);
                result.put("token", jwtToken);
                result.put("tokenType", "Bearer");
                result.put("expiresIn", jwtTokenUtil.getRemainingTime(jwtToken));
                result.put("userInfo", userInfo); // 这里的userInfo现在包含头像了
                if (systemUser != null) {
                    result.put("systemUser", systemUser);
                }
                result.put("timestamp", System.currentTimeMillis());

                log.info("返回给前端的完整结果: {}", result);
                return result;
            } else {
                String errmsg = (String) tokenData.get("errmsg");
                String errcode = String.valueOf(tokenData.get("errcode"));
                log.error("获取openid失败, errcode: {}, errmsg: {}", errcode, errmsg);
                throw new RuntimeException("获取openid失败: " + errmsg);
            }
        } catch (Exception e) {
            log.error("解析微信响应失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析微信响应失败", e);
        }
    }

    /**
     * 核心方法：创建/更新用户（适配sys_user表结构）
     * 基于openid唯一索引操作，包含头像处理
     */
    private User createOrUpdateUserWithOpenid(WeChatUserInfo userInfo) {
        try {
            if (userService != null) {
                User user = userService.createOrUpdateWechatUser(userInfo);
                if (user != null) {
                    return user;
                }
            }

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                    .eq(User::getOpenid, userInfo.getOpenid())
                    .eq(User::getDelFlag, "0"); // 未删除

            User existingUser = userMapper.selectOne(queryWrapper);

            if (existingUser != null) {
                existingUser.setNickName(userInfo.getNickname()); // 更新昵称
                existingUser.setSex(userInfo.getSex() != null ? userInfo.getSex().toString() : "2"); // 0男 1女 2未知

                // 🟢 更新头像：如果微信获取到了头像，则更新
                if (userInfo.getHeadimgurl() != null && !userInfo.getHeadimgurl().isEmpty()) {
                    existingUser.setAvatar(userInfo.getHeadimgurl());
                }

                existingUser.setUpdateBy("wechat_auth");
                existingUser.setUpdateTime(LocalDateTime.now());

                userMapper.updateById(existingUser);
                log.info("更新微信用户信息: userId={}, openid={}, nickname={}",
                        existingUser.getUserId(), existingUser.getOpenid(), userInfo.getNickname());
                return existingUser;
            } else {
                User newUser = User.builder()
                        .loginName("wx_" + userInfo.getOpenid().substring(0, 10)) // 生成唯一登录账号
                        .userName(userInfo.getNickname() != null ? userInfo.getNickname() : "微信用户") // 用户名称
                        .nickName(userInfo.getNickname() != null ? userInfo.getNickname() : "微信用户") // 昵称
                        .userType("01") // 01注册用户
                        .sex(userInfo.getSex() != null ? userInfo.getSex().toString() : "2") // 性别
                        .openid(userInfo.getOpenid()) // 插入openid
                        .status("0") // 0正常
                        .delFlag("0") // 0存在
                        .createBy("wechat_auth")
                        .createTime(LocalDateTime.now())
                        .updateBy("wechat_auth")
                        .updateTime(LocalDateTime.now())
                        .remark("微信授权自动注册用户，openid：" + userInfo.getOpenid())
                        // 🟢 插入头像
                        .avatar(userInfo.getHeadimgurl() != null ? userInfo.getHeadimgurl() : "")
                        .build();

                // 自增主键，无需手动设置userId
                userMapper.insert(newUser);
                log.info("新增微信用户: userId={}, openid={}, nickname={}",
                        newUser.getUserId(), newUser.getOpenid(), newUser.getNickName());
                return newUser;
            }
        } catch (Exception e) {
            // 捕获唯一索引冲突异常（openid重复）
            if (e.getMessage().contains("uk_openid")) {
                log.error("创建用户失败：openid={} 已存在", userInfo.getOpenid());
                throw new RuntimeException("该微信账号已绑定用户，请直接登录");
            }
            log.error("创建/更新用户失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建用户失败", e);
        }
    }

    /**
     * 刷新token接口
     */
    @PostMapping("/refresh-token")
    public Map<String, Object> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token格式错误");
        }

        String token = authHeader.substring(7);

        if (!jwtTokenUtil.validateToken(token)) {
            throw new RuntimeException("Token无效或已过期");
        }

        try {
            String newToken = jwtTokenUtil.refreshToken(token);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("token", newToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", jwtTokenUtil.getRemainingTime(newToken));
            result.put("message", "Token刷新成功");

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Token刷新失败", e);
        }
    }

    /**
     * 验证token接口
     */
    @GetMapping("/validate-token")
    public Map<String, Object> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token格式错误");
        }

        String token = authHeader.substring(7);
        boolean isValid = jwtTokenUtil.validateToken(token);
        boolean isExpired = jwtTokenUtil.isTokenExpired(token);
        String openid = jwtTokenUtil.getOpenidFromToken(token);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("valid", isValid);
        result.put("expired", isExpired);
        result.put("openid", openid);

        if (isValid && !isExpired) {
            result.put("message", "Token有效");
        } else if (isExpired) {
            result.put("message", "Token已过期");
        } else {
            result.put("message", "Token无效");
        }

        return result;
    }

    /**
     * 获取当前登录用户信息（测试用）
     */
    @GetMapping("/current-user")
    public Map<String, Object> getCurrentUser() {
        WeChatUserInfo currentUser = UserContext.getCurrentUser();

        Map<String, Object> result = new HashMap<>();
        if (currentUser != null) {
            result.put("success", true);
            result.put("userInfo", currentUser);
            result.put("source", "ThreadLocal");
        } else {
            result.put("success", false);
            result.put("message", "用户未登录");
        }
        return result;
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        // 清除Session中的用户信息
        request.getSession().removeAttribute("currentUser");

        // 清除ThreadLocal
        UserContext.clear();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "退出登录成功");
        return result;
    }

    /**
     * 微信用户信息封装类
     */
    public static class WeChatUserInfo {
        private String openid;
        private String nickname;
        private Integer sex;
        private String province;
        private String city;
        private String country;
        // 🟢 新增头像字段
        private String headimgurl;
        private java.util.List<String> privilege;
        private String unionid;
        private String accessToken;
        private Integer expiresIn;
        private String refreshToken;
        private String scope;
        private Long authTime;

        // getter和setter方法
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public Integer getSex() { return sex; }
        public void setSex(Integer sex) { this.sex = sex; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        // 🟢 头像的 Getter/Setter
        public String getHeadimgurl() { return headimgurl; }
        public void setHeadimgurl(String headimgurl) { this.headimgurl = headimgurl; }

        public java.util.List<String> getPrivilege() { return privilege; }
        public void setPrivilege(java.util.List<String> privilege) { this.privilege = privilege; }
        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public Integer getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public Long getAuthTime() { return authTime; }
        public void setAuthTime(Long authTime) { this.authTime = authTime; }

        @Override
        public String toString() {
            return "WeChatUserInfo{" +
                    "openid='" + openid + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", sex=" + sex +
                    ", headimgurl='" + headimgurl + '\'' + // 日志包含头像
                    ", province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    ", accessToken='" + accessToken + '\'' +
                    ", expiresIn=" + expiresIn +
                    ", authTime=" + authTime +
                    '}';
        }
    }

    /**
     * 请求参数类
     */
    public static class CodeRequest {
        private String code;
        private String state;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        @Override
        public String toString() {
            return "CodeRequest{code='" + code + "', state='" + state + "'}";
        }
    }

    /**
     * 生成随机的state参数
     */
    private String generateRandomState() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 保存用户信息到数据库（简化版，仅日志）
     */
    private void saveUserInfo(WeChatUserInfo userInfo) {
        log.info("=== 保存用户信息 ===");
        log.info("OpenID: {}", userInfo.getOpenid());
        log.info("昵称: {}", userInfo.getNickname());
        log.info("头像: {}", userInfo.getHeadimgurl());
        log.info("授权时间: {}", userInfo.getAuthTime());
        log.info("=========================");
    }
}
