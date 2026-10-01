package com.wuye.points.util;

import com.wuye.points.exception.BizException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JwtUtil 纯单元测试（不加载 Spring 上下文）。
 * 通过反射注入与测试配置相同的密钥与过期时间，验证令牌的生成与解析。
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        setField("secret", "test-secret-key-for-unit-testing-only-32bytes!!");
        setField("accessTokenExpiration", 3_600_000L);
        setField("refreshTokenExpiration", 604_800_000L);
        jwtUtil.init();
    }

    private void setField(String name, Object value) throws Exception {
        Field f = JwtUtil.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(jwtUtil, value);
    }

    @Test
    void generateAndParseAccessToken_roundTrip() {
        String token = jwtUtil.generateAccessToken("u1", "resident", "pc1", "fam1");
        assertNotNull(token);

        Claims claims = jwtUtil.parseToken(token);
        assertEquals("u1", claims.getSubject());
        assertEquals("resident", claims.get("role"));
        assertEquals("pc1", claims.get("propertyCompanyId"));
        assertEquals("fam1", claims.get("familyId"));
    }

    @Test
    void generateAndParseRefreshToken_roundTrip() {
        String token = jwtUtil.generateRefreshToken("u1");

        Claims claims = jwtUtil.parseRefreshToken(token);
        assertEquals("u1", claims.getSubject());
        assertEquals("refresh", claims.get("type"));
    }

    @Test
    void parseToken_invalidToken_throwsBizException() {
        assertThrows(BizException.class, () -> jwtUtil.parseToken("not.a.valid.token"));
    }

    @Test
    void parseToken_emptyToken_throwsBizException() {
        assertThrows(BizException.class, () -> jwtUtil.parseToken(""));
    }
}
