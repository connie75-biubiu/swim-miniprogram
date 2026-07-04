package com.swim.config;

import com.swim.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final JwtUtil jwtUtil;
    private final RequestLogInterceptor requestLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 请求日志放最前，确保任何请求（含鉴权失败的）都有日志
        registry.addInterceptor(requestLogInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    return true;
                }
                String auth = request.getHeader("Authorization");
                if (auth == null || !auth.startsWith("Bearer ")) {
                    response.setStatus(401);
                    return false;
                }
                try {
                    Long userId = jwtUtil.parseUserId(auth.substring(7));
                    request.setAttribute("userId", userId);
                    return true;
                } catch (Exception e) {
                    response.setStatus(401);
                    return false;
                }
            }
        }).addPathPatterns("/api/**").excludePathPatterns("/api/auth/wx-login");
    }
}
