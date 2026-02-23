package com.zjy.xgxt.interceptor;
import com.zjy.xgxt.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求 (处理跨域预检请求)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头中获取 token (你的前端 request.js 已经设置了 Authorization)
        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("无访问权限，请先登录");
        }

        // 如果前端传的 token 带有 "Bearer " 前缀，去掉它
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 校验 token
        Claims claims = JwtUtils.parseToken(token);
        if (claims == null) {
            throw new RuntimeException("Token已过期或无效，请重新登录");
        }

        // 校验通过，放行
        return true;
    }
}
