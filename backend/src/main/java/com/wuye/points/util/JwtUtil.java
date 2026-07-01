package com.wuye.points.util;

import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(String userId, String role, String propertyCompanyId, String familyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("propertyCompanyId", propertyCompanyId);
        if (familyId != null) {
            claims.put("familyId", familyId);
        }
        claims.put("type", "access");
        return buildToken(userId, claims, accessTokenExpiration);
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return buildToken(userId, claims, refreshTokenExpiration);
    }

    private String buildToken(String subject, Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return parseTokenClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BizException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }
    }

    public Claims parseTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 解析 Refresh Token，区分过期与无效
     */
    public Claims parseRefreshToken(String token) {
        Claims claims;
        try {
            claims = parseTokenClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BizException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }
        if (claims.getExpiration().before(new Date())) {
            throw new BizException(ErrorCode.TOKEN_EXPIRED);
        }
        return claims;
    }

    public void validateAccessToken(Claims claims) {
        if (!"access".equals(claims.get("type", String.class))) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }
        if (claims.getExpiration().before(new Date())) {
            throw new BizException(ErrorCode.TOKEN_EXPIRED);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseTokenClaims(token);
            validateAccessToken(claims);
            return true;
        } catch (BizException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public String getPropertyCompanyId(String token) {
        return parseToken(token).get("propertyCompanyId", String.class);
    }

    public String getFamilyId(String token) {
        return parseToken(token).get("familyId", String.class);
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
