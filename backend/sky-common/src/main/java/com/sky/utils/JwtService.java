package com.sky.utils;

import com.sky.constant.JwtClaimsConstant;
import com.sky.enumeration.UserType;
import com.sky.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌服务 —— 创建、解析、失效
 */
@Component
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;
    private SecretKey adminSecretKey;
    private SecretKey userSecretKey;

    @Autowired
    public JwtService(JwtProperties jwtProperties, StringRedisTemplate redisTemplate) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
        initKeys();
    }

    /**
     * 预计算签名密钥，避免每次请求重建
     */
    private void initKeys() {
        String adminKey = jwtProperties.getAdmin().getSecretKey();
        String userKey = jwtProperties.getUser().getSecretKey();
        // HS256 要求密钥至少 256 bits (32 bytes)；不足则补 SHA-256 哈希得到固定长度的安全密钥
        this.adminSecretKey = Keys.hmacShaKeyFor(buildKeyBytes(adminKey));
        this.userSecretKey = Keys.hmacShaKeyFor(buildKeyBytes(userKey));
    }

    /**
     * 将配置的密钥字符串转为固定长度的安全密钥字节
     */
    private byte[] buildKeyBytes(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    // ==================== Token 创建 ====================

    /**
     * 生成管理端 JWT 令牌
     */
    public String createAdminToken(Long empId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, empId);
        return createToken(adminSecretKey, jwtProperties.getAdmin().getTtl(), claims);
    }

    /**
     * 生成用户端 JWT 令牌
     */
    public String createUserToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        return createToken(userSecretKey, jwtProperties.getUser().getTtl(), claims);
    }

    /**
     * 底层 token 生成
     */
    private String createToken(SecretKey key, long ttlMillis, Map<String, Object> claims) {
        long expMillis = System.currentTimeMillis() + ttlMillis;
        return Jwts.builder()
                .claims(claims)
                .signWith(key)
                .expiration(new Date(expMillis))
                .compact();
    }

    // ==================== Token 解析 ====================

    /**
     * 解析 JWT 令牌
     */
    public Claims parseToken(String token, UserType userType) {
        SecretKey key = (userType == UserType.ADMIN) ? adminSecretKey : userSecretKey;
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 token 中提取指定 claim 的 Long 值
     */
    public Long extractUserId(String token, UserType userType) {
        Claims claims = parseToken(token, userType);
        String claimKey = (userType == UserType.ADMIN) ? JwtClaimsConstant.EMP_ID : JwtClaimsConstant.USER_ID;
        Object value = claims.get(claimKey);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }

    // ==================== Token 黑名单（退出登录） ====================

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * 将 token 加入黑名单，有效期 = token 剩余有效时间
     */
    public void invalidateToken(String token, UserType userType) {
        try {
            Claims claims = parseToken(token, userType);
            Date expiration = claims.getExpiration();
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            if (remainingMs > 0) {
                String hash = sha256Hex(token);
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + hash,
                        "1",
                        Duration.ofMillis(remainingMs)
                );
                log.info("Token 已加入黑名单, 剩余有效期: {}ms", remainingMs);
            }
        } catch (Exception e) {
            // token 已过期或无效，无需加入黑名单
            log.warn("Token 无效或已过期，跳过黑名单: {}", e.getMessage());
        }
    }

    /**
     * 检查 token 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String hash = sha256Hex(token);
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + hash));
        } catch (Exception e) {
            log.warn("Redis 黑名单检查失败，默认放行: {}", e.getMessage());
            return false;
        }
    }

    // ==================== 工具方法 ====================

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
