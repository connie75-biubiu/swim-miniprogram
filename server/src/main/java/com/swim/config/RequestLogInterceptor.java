package com.swim.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 记录每个 HTTP 请求：方法、URI、查询参数、状态码、耗时。
 * 排查 API 问题时，这条日志能立刻看出"请求有没有到后端、走到哪、花了多久"。
 */
@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final String START_ATTR = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_ATTR, System.currentTimeMillis());
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        // 非敏感接口才打印查询参数详情；这里全量打印方便调试，生产可按需收敛
        log.info("-> {} {}{}", request.getMethod(), request.getRequestURI(), query);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Object startObj = request.getAttribute(START_ATTR);
        long cost = startObj != null ? System.currentTimeMillis() - (Long) startObj : -1;
        int status = response.getStatus();
        String headers = Collections.list(request.getHeaderNames()).stream()
                .filter(h -> !"authorization".equalsIgnoreCase(h))
                .map(h -> h + "=" + request.getHeader(h))
                .collect(Collectors.joining(", "));
        if (ex != null) {
            log.error("<- {} {} status={} cost={}ms ex={}",
                    request.getMethod(), request.getRequestURI(), status, cost, ex.toString());
        } else if (status >= 400) {
            log.warn("<- {} {} status={} cost={}ms", request.getMethod(), request.getRequestURI(), status, cost);
        } else {
            log.info("<- {} {} status={} cost={}ms", request.getMethod(), request.getRequestURI(), status, cost);
        }
    }
}
