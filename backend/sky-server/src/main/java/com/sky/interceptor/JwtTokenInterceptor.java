package com.sky.interceptor;

import com.sky.context.BaseContext;
import com.sky.enumeration.UserType;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 令牌校验拦截器 —— 统一处理管理端和用户端请求
 */
@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非 Controller 方法（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String path = request.getRequestURI();

        // 根据请求路径前缀判断用户类型
        UserType userType;
        String tokenName;
        if (path.startsWith("/admin")) {
            userType = UserType.ADMIN;
            tokenName = jwtProperties.getAdmin().getTokenName();
        } else if (path.startsWith("/user")) {
            userType = UserType.USER;
            tokenName = jwtProperties.getUser().getTokenName();
        } else {
            // 未匹配的路径直接放行
            return true;
        }

        // 1. 从请求头获取 token
        String token = request.getHeader(tokenName);
        if (token == null || token.isEmpty()) {
            log.warn("{} 端请求缺少 token, path: {}", userType, path);
            response.setStatus(401);
            return false;
        }

        // 2. 检查黑名单
        if (jwtService.isTokenBlacklisted(token)) {
            log.warn("{} 端 token 已被拉黑, path: {}", userType, path);
            response.setStatus(401);
            return false;
        }

        // 3. 校验并解析 token
        try {
            Long userId = jwtService.extractUserId(token, userType);
            BaseContext.setCurrentUser(new BaseContext.LoginUser(userId, userType));
            log.info("{} 端 jwt 校验通过, userId: {}", userType, userId);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("{} 端 token 已过期: {}", userType, e.getMessage());
        } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.warn("{} 端 token 无效: {}", userType, e.getMessage());
        } catch (Exception e) {
            log.error("{} 端 token 校验异常: {}", userType, e.getMessage(), e);
        }

        response.setStatus(401);
        return false;
    }
}
