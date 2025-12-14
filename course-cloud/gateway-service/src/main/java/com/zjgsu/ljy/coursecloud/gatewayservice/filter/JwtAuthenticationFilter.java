package com.zjgsu.ljy.coursecloud.gatewayservice.filter;

import com.zjgsu.ljy.coursecloud.gatewayservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器
 * 拦截所有请求，验证 JWT Token，并将用户信息传递给后端服务
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // 白名单：不需要认证的路径
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/register/student",
            "/api/auth/register/teacher",
            "/auth/login",
            "/auth/register",
            "/auth/register/student",
            "/auth/register/teacher"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("请求路径: {}", path);

        // 1. 白名单路径直接放行
        if (isWhiteListed(path)) {
            log.debug("白名单路径，直接放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 获取 Authorization 请求头
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("未提供有效的 Authorization 请求头");
            return unauthorized(exchange.getResponse());
        }

        // 3. 提取 Token (去掉 "Bearer " 前缀)
        String token = authHeader.substring(7);

        // 4. 验证 Token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token 验证失败");
            return unauthorized(exchange.getResponse());
        }

        // 5. 解析 Token 获取用户信息
        try {
            Claims claims = jwtUtil.parseToken(token);
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            log.info("用户 {} (ID: {}, 角色: {}) 通过认证", username, userId, role);

            // 6. 将用户信息添加到请求头，传递给后端服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .header("X-User-Role", role)
                    .build();

            // 7. 使用修改后的请求继续过滤器链
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            log.error("解析 Token 失败: {}", e.getMessage());
            return unauthorized(exchange.getResponse());
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 返回 401 未认证响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    /**
     * 过滤器优先级：数字越小优先级越高
     * -100 表示在大多数过滤器之前执行
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
