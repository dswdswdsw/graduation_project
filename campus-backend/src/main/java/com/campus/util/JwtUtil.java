package com.campus.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT (JSON Web Token) 工具类
 * 
 * <p>提供JWT令牌的生成、解析和验证功能。
 * 用于用户身份认证的无状态会话管理。</p>
 * 
 * <h2>JWT结构：</h2>
 * <pre>
 * Header.Payload.Signature
 * </pre>
 * 
 * <h2>Token中存储的信息（Payload）：</h2>
 * <ul>
 *   <li><b>userId</b> - 用户ID</li>
 *   <li><b>role</b> - 用户角色</li>
 *   <li><b>iat</b> - 签发时间（自动生成）</li>
 *   <li><b>exp</b> - 过期时间（自动生成）</li>
 * </ul>
 * 
 * <h2>安全特性：</h2>
 * <ul>
 *   <li>使用HMAC-SHA256算法签名，防止篡改</li>
 *   <li>配置化的密钥和过期时间</li>
 *   <li>支持Bearer Token格式自动去除前缀</li>
 * </ul>
 * 
 * <h2>使用示例：</h2>
 * <pre>
 * // 生成Token
 * String token = jwtUtil.generateToken(userId, "admin");
 * 
 * // 解析用户ID
 * Long userId = jwtUtil.getUserIdFromToken(token);
 * 
 * // 验证Token有效性
 * boolean valid = jwtUtil.validateToken(token);
 * </pre>
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT签名密钥
     * 从application.yml配置文件中读取：jwt.secret
     * 应该使用足够长且复杂的字符串，生产环境建议从环境变量读取
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token过期时间（毫秒）
     * 从application.yml配置文件中读取：jwt.expiration
     * 默认值：86400000ms（24小时）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT令牌
     * 
     * <p>将用户ID和角色信息编码到Token中，
     * 设置签发时间和过期时间，并使用HS256算法签名。</p>
     * 
     * @param userId 用户ID
     * @param role 用户角色（admin/teacher/student）
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT令牌（可带或不带"Bearer "前缀）
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户角色
     * 
     * @param token JWT令牌（可带或不带"Bearer "前缀）
     * @return 用户角色字符串
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证Token是否有效
     * 
     * <p>检查Token是否可以正常解析，包括：
     * <ul>
     *   <li>签名是否正确</li>
     *   <li>是否已过期</li>
     *   <li>格式是否合法</li>
     * </ul></p>
     * 
     * @param token JWT令牌
     * @return true表示有效，false表示无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 解析Token并返回Claims对象
     * 
     * <p>内部方法，支持自动去除"Bearer "前缀。</p>
     * 
     * @param token JWT令牌
     * @return Claims对象，包含所有声明信息
     * @throws Exception 当Token无效、过期或签名不匹配时抛出异常
     */
    private Claims parseToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}
