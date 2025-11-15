package com.microservice.skeleton.user.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

        @RestController
        public class WechatTokenController {

            // 微信获取access_token的官方接口地址
            private static final String WECHAT_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

            // AppSecret安全提醒内容
            private static final String APP_SECRET_REMINDER = "【AppSecret安全提醒】\n" +
                    "AppSecret是账号使用后台API接口的小程序密钥，请开发者妥善保管，避免因泄露造成账号被其它人冒用等风险。\n" +
                    "如长期无AppSecret的使用需求，开发者可以使用管理员账号登录小程序平台，在“开发-开发管理”中对AppSecret进行冻结，提高账号的安全性。\n" +
                    "AppSecret冻结后，开发者无法使用AppSecret获取Access token（接口返回错误码40243），不影响账号基本功能的正常使用，不影响通过第三方授权调用后台接口，不影响云开发调用后台接口。\n" +
                    "开发者可以随时使用管理员账号登录小程序平台，在“开发-开发管理”中对AppSecret进行解冻。";

            // 注入Spring自带的RestTemplate（无需额外配置，直接可用）
            @Resource
            private RestTemplate restTemplate;

            /**
             * 测试接口：调用微信接口获取access_token，并返回安全提醒
             * @param appid 小程序唯一标识
             * @param secret 小程序的AppSecret（密钥）
             * @return 包含安全提醒和微信接口响应的结果
             */
            @GetMapping("/test/access-token")
            public JSONObject getAccessToken(
                    @RequestParam String appid,
                    @RequestParam String secret) {

                // 构建微信接口请求参数
                String requestUrl = WECHAT_TOKEN_URL +
                        "?grant_type=client_credential" +
                        "&appid=" + appid +
                        "&secret=" + secret;

        // 发起HTTPS请求到微信接口
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(requestUrl, String.class);
        JSONObject wechatResponse = JSONObject.parseObject(responseEntity.getBody());

        // 构建最终响应结果
        JSONObject result = new JSONObject();
        result.put("security_reminder", APP_SECRET_REMINDER);
        result.put("wechat_response", wechatResponse);

        return result;
    }
}
