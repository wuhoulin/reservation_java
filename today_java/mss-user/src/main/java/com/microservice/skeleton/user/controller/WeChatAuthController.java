package com.microservice.skeleton.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.skeleton.user.domain.entity.User.User;
import com.microservice.skeleton.user.service.UserService;
import com.microservice.skeleton.user.service.UserWechatService;
import com.microservice.skeleton.user.util.JwtTokenUtil;
import com.microservice.skeleton.user.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    /**
     * 生成微信授权URL
     */
    @GetMapping("/generate-auth-url")
    public Map<String, String> generateAuthUrl(
            @RequestParam String redirectPath,
            @RequestParam(defaultValue = "snsapi_userinfo") String scope) { // 默认使用snsapi_userinfo

        try {
            String redirectUri = "http://ndnu-yuyue.xyz" + redirectPath;

            System.out.println("=== 微信授权详细调试 ===");
            System.out.println("AppId: " + appId);
            System.out.println("授权范围: " + scope);
            System.out.println("前端路径: " + redirectPath);
            System.out.println("生成的redirect_uri: " + redirectUri);

            String encodedRedirectUri = URLEncoder.encode(redirectUri, "UTF-8");
            System.out.println("编码后的redirect_uri: " + encodedRedirectUri);

            String state = generateRandomState();
            System.out.println("生成的state: " + state);

            String authUrl = String.format(
                    "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s#wechat_redirect",
                    appId, encodedRedirectUri, scope, state);

            System.out.println("完整的授权URL: " + authUrl);
            System.out.println("=========================");

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
            System.out.println("错误: code参数为空或为空字符串");
            throw new RuntimeException("code参数不能为空");
        }

        // 第一步：通过code获取access_token和openid
        String tokenUrl = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        System.out.println("请求微信token URL: " + tokenUrl);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String tokenResponse = restTemplate.getForObject(tokenUrl, String.class);

            System.out.println("微信token响应: " + tokenResponse);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> tokenData = mapper.readValue(tokenResponse, new TypeReference<Map<String, Object>>() {});

            if (tokenData.containsKey("openid")) {
                String openid = (String) tokenData.get("openid");
                String accessToken = (String) tokenData.get("access_token");
                String refreshToken = (String) tokenData.get("refresh_token");
                Integer expiresIn = (Integer) tokenData.get("expires_in");
                String scope = (String) tokenData.get("scope");

                System.out.println("成功获取openid: " + openid);
                System.out.println("access_token: " + accessToken);
                System.out.println("scope: " + scope);

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

                        System.out.println("请求用户信息URL: " + userInfoUrl);

                        String userInfoResponse = restTemplate.getForObject(userInfoUrl, String.class);
                        System.out.println("用户信息响应: " + userInfoResponse);

                        Map<String, Object> userInfoData = mapper.readValue(userInfoResponse,
                                new TypeReference<Map<String, Object>>() {});

                        if (userInfoData.containsKey("nickname")) {
                            // 成功获取用户信息
                            userInfo.setNickname((String) userInfoData.get("nickname"));
                            userInfo.setSex((Integer) userInfoData.get("sex"));
                            userInfo.setProvince((String) userInfoData.get("province"));
                            userInfo.setCity((String) userInfoData.get("city"));
                            userInfo.setCountry((String) userInfoData.get("country"));
                            userInfo.setHeadimgurl((String) userInfoData.get("headimgurl"));
                            userInfo.setUnionid((String) userInfoData.get("unionid"));

                            System.out.println("成功获取用户详细信息:");
                            System.out.println("昵称: " + userInfo.getNickname());
                            System.out.println("头像: " + userInfo.getHeadimgurl());

                            // 关键：自动创建或更新系统用户和微信用户关联
                            systemUser = userService.createOrUpdateWechatUser(userInfo);
                            userWechatService.createOrUpdateWechatUser(systemUser.getUserId(), userInfo);

                            log.info("微信用户授权成功: userId={}, openid={}, nickname={}",
                                    systemUser.getUserId(), openid, userInfo.getNickname());
                        } else {
                            System.out.println("获取用户详细信息失败: " + userInfoData.get("errmsg"));
                            // 即使没有获取到详细信息，也尝试创建基础用户
                            systemUser = userService.createOrUpdateWechatUser(userInfo);
                        }
                    } catch (Exception e) {
                        System.out.println("获取用户详细信息异常: " + e.getMessage());
                        // 异常情况下也尝试创建基础用户
                        systemUser = userService.createOrUpdateWechatUser(userInfo);
                    }
                } else {
                    System.out.println("当前授权范围: " + scope + "，无法获取用户详细信息");
                    // 静默授权情况下创建基础用户
                    systemUser = userService.createOrUpdateWechatUser(userInfo);
                }

                // 生成JWT token
                Map<String, Object> claims = new HashMap<>();
                claims.put("openid", openid);
                claims.put("authTime", userInfo.getAuthTime());
                claims.put("scope", scope);
                claims.put("nickname", userInfo.getNickname());
                claims.put("headimgurl", userInfo.getHeadimgurl());
                if (systemUser != null) {
                    claims.put("userId", systemUser.getUserId());
                    claims.put("userName", systemUser.getUserName());
                }

                String jwtToken = jwtTokenUtil.generateToken(openid, claims);
                System.out.println("生成的JWT Token: " + jwtToken);

                // 将用户信息存入Session
                httpRequest.getSession().setAttribute("currentUser", userInfo);
                System.out.println("用户信息已存入Session: " + userInfo);

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
                result.put("userInfo", userInfo);
                if (systemUser != null) {
                    result.put("systemUser", systemUser);
                }
                result.put("timestamp", System.currentTimeMillis());

                System.out.println("返回给前端的完整结果: " + result);
                return result;
            } else {
                String errmsg = (String) tokenData.get("errmsg");
                String errcode = String.valueOf(tokenData.get("errcode"));
                System.out.println("获取openid失败, errcode: " + errcode + ", errmsg: " + errmsg);
                throw new RuntimeException("获取openid失败: " + errmsg);
            }
        } catch (Exception e) {
            System.out.println("解析微信响应失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("解析微信响应失败", e);
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
        private String headimgurl;
        private java.util.List<String> privilege;
        private String unionid;
        private String accessToken;
        private Integer expiresIn;
        private String refreshToken;
        private String scope;
        private Long authTime;

        // getter和setter方法...
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
                    ", province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    ", headimgurl='" + headimgurl + '\'' +
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

    private void saveUserInfo(WeChatUserInfo userInfo) {
        // 保存用户信息到数据库的实现
        System.out.println("=== 保存用户信息 ===");
        System.out.println("OpenID: " + userInfo.getOpenid());
        System.out.println("授权时间: " + userInfo.getAuthTime());
        System.out.println("=========================");
    }
}
