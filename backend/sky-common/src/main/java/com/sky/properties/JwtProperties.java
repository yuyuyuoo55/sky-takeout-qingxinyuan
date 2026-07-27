package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private AdminConfig admin = new AdminConfig();

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private UserConfig user = new UserConfig();

    @Data
    public static class AdminConfig {
        private String secretKey;
        private long ttl = 7200000;
        private String tokenName = "token";
    }

    @Data
    public static class UserConfig {
        private String secretKey;
        private long ttl = 7200000;
        private String tokenName = "authentication";
    }

}
