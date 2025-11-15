package com.microservice.skeleton.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wechat")
public class WeChatAuthController {

    @Value("${wechat.appId}")
    private String appId;

    @Value("${wechat.appSecret}")
    private String appSecret;

    /**
     * 生成微信授权URL
     * @param redirectPath 前端回调页面的路径（如：/auth-callback）
     * @param scope 授权作用域（snsapi_base 或 snsapi_userinfo）
     * @return 微信授权URL
     */
    @GetMapping("/generate-auth-url")
    public Map<String, String> generateAuthUrl(
            @RequestParam String redirectPath,
            @RequestParam(defaultValue = "snsapi_base") String scope) {

        try {
            // 使用HTTP协议（你目前是HTTP）
            String redirectUri = "http://ndnu-yuyue.xyz" + redirectPath; // 去掉:9062端口

            System.out.println("=== 微信授权详细调试 ===");
            System.out.println("AppId: " + appId);
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
            result.put("debug_redirectUri", redirectUri);
            result.put("debug_encodedRedirectUri", encodedRedirectUri);

            return result;

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL编码失败", e);
        }
    }

    /**
     * 生成随机的state参数
     */
    private String generateRandomState() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 使用code换取openid
     */
    @PostMapping("/exchange-code")
    public Map<String, Object> exchangeCode(@RequestBody CodeRequest request) {
        // 使用CodeRequest类接收参数
        String code = request.getCode();
        String state = request.getState();

        System.out.println("=== 完整请求体调试 ===");
        System.out.println("接收到的request: " + request);
        System.out.println("接收到的code: " + code);
        System.out.println("接收到的state: " + state);

        // 检查code是否为空
        if (code == null || code.trim().isEmpty()) {
            System.out.println("错误: code参数为空或为空字符串");
            throw new RuntimeException("code参数不能为空");
        }

        // 构造请求URL
        String url = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        System.out.println("请求微信URL: " + url);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            System.out.println("微信返回的响应: " + response);

            // 解析响应
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseData = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});

            if (responseData.containsKey("openid")) {
                String openid = (String) responseData.get("openid");
                String accessToken = (String) responseData.get("access_token");

                System.out.println("成功获取openid: " + openid);
                System.out.println("access_token: " + accessToken);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("openid", openid);
                result.put("accessToken", accessToken);

                // 这里可以保存用户信息到数据库
                saveUserInfo(openid);

                return result;
            } else {
                String errmsg = (String) responseData.get("errmsg");
                String errcode = String.valueOf(responseData.get("errcode"));
                System.out.println("获取openid失败, errcode: " + errcode + ", errmsg: " + errmsg);
                throw new RuntimeException("获取openid失败: " + errmsg);
            }
        } catch (Exception e) {
            System.out.println("解析微信响应失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("解析微信响应失败", e);
        }
    }

    // 确保CodeRequest类存在且有getter/setter
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
     * 验证state参数 - 临时返回true
     */
    private boolean isValidState(String state) {
        // 临时返回true，先确保能获取openid
        // 生产环境需要实现完整的state验证逻辑
        return true;
    }

    private void saveUserInfo(String openid) {
        // 实现保存用户信息的逻辑
        // userService.saveWeChatUser(openid);
        System.out.println("保存用户信息: " + openid);
    }

}
