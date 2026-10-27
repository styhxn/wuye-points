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

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 居民角色提权回归（A4）。
 *
 * <p>历史问题：物业管理员/住户本人可通过 PUT /residents/{id} 把 role 改成 platform_admin，
 * 再 refresh token 完成提权。本测试使用真实拦截器 + H2 验证：角色变更只有平台管理员可做。</p>
 */
@DisplayName("居民角色修改权限")
class ResidentRoleSecurityTest extends ControllerTestBase {

    private static final String TARGET_ID = "res_role_target_01";

    @Autowired
    private ResidentRepository residentRepository;

    @AfterEach
    void cleanUp() {
        residentRepository.findById(TARGET_ID).ifPresent(residentRepository::delete);
    }

    private void createTarget(String role) {
        Resident target = new Resident();
        target.setId(TARGET_ID);
        target.setPhone("13900008888");
        target.setName("角色测试住户");
        target.setRole(role);
        target.setStatus("active");
        target.setPropertyCompanyId("pc_role");
        target.setPointBalance(0);
        target.setCoinBalance(BigDecimal.ZERO);
        residentRepository.save(target);
    }

    @Test
    @DisplayName("物业管理员把住户改成 platform_admin → 403，角色不变")
    void propertyAdmin_cannotEscalateRole() throws Exception {
        createTarget("resident");

        mockMvc.perform(put("/residents/" + TARGET_ID)
                        .header("Authorization", bearer("res_prop_admin", "property_admin", "pc_role", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"platform_admin\"}"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        assertEquals("resident", residentRepository.findById(TARGET_ID).orElseThrow().getRole());
    }

    @Test
    @DisplayName("住户通过 /residents/me 改自己的 role → 403")
    void resident_cannotChangeOwnRole() throws Exception {
        createTarget("resident");

        mockMvc.perform(put("/residents/me")
                        .header("Authorization", bearer(TARGET_ID, "resident", "pc_role", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"platform_admin\"}"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.message", containsString("不支持修改角色")));

        assertEquals("resident", residentRepository.findById(TARGET_ID).orElseThrow().getRole());
    }

    @Test
    @DisplayName("平台管理员可以修改角色")
    void platformAdmin_canChangeRole() throws Exception {
        createTarget("resident");

        mockMvc.perform(put("/residents/" + TARGET_ID)
                        .header("Authorization", bearer("res_platform_admin", "platform_admin", "pc_role", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"platform_admin\"}"))
                .andExpect(status().isOk());

        assertEquals("platform_admin", residentRepository.findById(TARGET_ID).orElseThrow().getRole());
    }
}
