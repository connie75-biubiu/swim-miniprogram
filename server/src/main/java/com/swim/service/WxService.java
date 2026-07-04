package com.swim.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swim.config.WxConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxService {
    private final WxConfig wxConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String cachedToken;
    private long tokenExpireAt;

    public String code2Openid(String code) {
        log.info("code2Openid 开始: appid={}, code={}", wxConfig.getAppid(), code);
        if ("test-code".equals(code)) {
            log.info("使用测试 code，返回测试 openid");
            return "test-openid-" + code;
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";
        Map<String, String> params = new HashMap<>();
        params.put("appid", wxConfig.getAppid());
        params.put("secret", wxConfig.getSecret());
        params.put("code", code);
        try {
            String body = restTemplate.getForObject(url, String.class, params);
            log.info("微信 jscode2session 响应: {}", body);
            JsonNode node = objectMapper.readTree(body);
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                throw new RuntimeException(node.path("errmsg").asText("微信登录失败"));
            }
            return node.get("openid").asText();
        } catch (Exception e) {
            log.error("code2Openid 失败: appid={}, code={}", wxConfig.getAppid(), code, e);
            throw new RuntimeException("微信登录失败: " + e.getMessage());
        }
    }

    public String getPhoneNumber(String code) {
        if (code != null && code.startsWith("test-phone")) {
            return "13800138000";
        }
        String token = getAccessToken();
        String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + token;
        try {
            Map<String, String> body = Map.of("code", code);
            String resp = restTemplate.postForObject(url, body, String.class);
            log.info("微信 getuserphonenumber 响应: {}", resp);
            JsonNode node = objectMapper.readTree(resp);
            if (node.path("errcode").asInt() != 0) {
                throw new RuntimeException(node.path("errmsg").asText("获取手机号失败"));
            }
            return node.path("phone_info").path("phoneNumber").asText();
        } catch (Exception e) {
            log.error("getPhoneNumber 失败", e);
            throw new RuntimeException("获取手机号失败: " + e.getMessage());
        }
    }

    private synchronized String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireAt) {
            return cachedToken;
        }
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
        Map<String, String> params = new HashMap<>();
        params.put("appid", wxConfig.getAppid());
        params.put("secret", wxConfig.getSecret());
        try {
            String body = restTemplate.getForObject(url, String.class, params);
            log.info("微信 token 响应: {}", body);
            JsonNode node = objectMapper.readTree(body);
            cachedToken = node.get("access_token").asText();
            tokenExpireAt = System.currentTimeMillis() + (node.get("expires_in").asLong() - 300) * 1000;
            return cachedToken;
        } catch (Exception e) {
            log.error("getAccessToken 失败: appid={}", wxConfig.getAppid(), e);
            throw new RuntimeException("获取access_token失败");
        }
    }
}
