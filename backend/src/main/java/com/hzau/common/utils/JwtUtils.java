package com.hzau.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.common.utils
 * @className: JwtUtils
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/22 下午3:29
 */

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret; // 密钥

    @Value("${jwt.expiration}")
    private Long expiration; // 过期时间

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 根据用户名和用户ID生成JWT, token
     * @param username
     * @param userId
     * @return
     */
    public String generateToken(String username, Long userId) {

        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从token中获取用户名
     * @param token
     * @return
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从token中获取用户ID
     * @param token
     * @return
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userIdObj = claims.get("userId");
        
        if (userIdObj == null) {
            throw new RuntimeException("UserId not found in token");
        }
        
        // 更安全的类型转换
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        } else {
            throw new RuntimeException("Invalid userId type in token: " + userIdObj.getClass());
        }
    }

    /**
     * 验证token
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            log.error("validateToken error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从token中获取Claims
     * @param token
     * @return
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

