package com.wuye.points.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试用 JWT 工厂。与 {@code JwtUtil} 使用相同的测试密钥与生产 claim 命名
 * （role / propertyCompanyId / familyId / type），用于 Controller 集成测试中
 * 构造各角色的身份令牌。注意：密钥必须与 src/test/resources/application.yml 中的
 * jwt.secret 保持一致。
 */
public final class TestTokenFactory {

    public static final String SECRET = "test-secret-key-for-unit-testing-only-32bytes!!";

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private TestTokenFactory() {
    }

    public static String accessToken(String userId, String role, String propertyCompanyId, String familyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("propertyCompanyId", propertyCompanyId);
        if (familyId != null) {
            claims.put("familyId", familyId);
        }
        claims.put("type", "access");
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 3600_000L))
                .signWith(KEY)
                .compact();
    }

    public static String accessToken(String userId, String role) {
        return accessToken(userId, role, null, null);
    }
}
