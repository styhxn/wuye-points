package com.wuye.points.controller;

import com.wuye.points.entity.Resident;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 登出 / token 版本失效回归（B3）。
 */
@DisplayName("登出后旧令牌失效")
class LogoutRevocationTest extends ControllerTestBase {

    private static final String USER_ID = "res_logout_test";

    @Autowired
    private ResidentRepository residentRepository;

    @AfterEach
    void cleanUp() {
        residentRepository.findById(USER_ID).ifPresent(residentRepository::delete);
    }

    private void createResident() {
        Resident resident = new Resident();
        resident.setId(USER_ID);
        resident.setPhone("13900007777");
        resident.setName("登出测试");
        resident.setRole("resident");
        resident.setUserType("owner");
        resident.setPropertyCompanyId("pc1");
        resident.setStatus("active");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        residentRepository.save(resident);
    }

    @Test
    @DisplayName("登出后旧 access token → 401")
    void logout_invalidatesAccessToken() throws Exception {
        createResident();
        String accessToken = jwtUtil.generateAccessToken(USER_ID, "resident", "pc1", null, 0);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/auth/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

        assertEquals(1, residentRepository.findById(USER_ID).orElseThrow().getTokenVersion());
    }

    @Test
    @DisplayName("登出后旧 refresh token → 401")
    void logout_invalidatesRefreshToken() throws Exception {
        createResident();
        String accessToken = jwtUtil.generateAccessToken(USER_ID, "resident", "pc1", null, 0);
        String refreshToken = jwtUtil.generateRefreshToken(USER_ID, 0);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }
}
