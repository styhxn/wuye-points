package com.wuye.points.controller;

import com.wuye.points.payment.WalletCardService;
import com.wuye.points.payment.WalletCardsDTO;
import com.wuye.points.service.PlatformSettingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 管理端资金配置与钱包卡权限回归（A5/A6）。
 */
@DisplayName("资金配置/钱包卡权限")
class AdminConfigPermissionTest extends ControllerTestBase {

    @MockBean
    private PlatformSettingService platformSettingService;
    @MockBean
    private WalletCardService walletCardService;

    @Test
    @DisplayName("结算配置: 物业管理员可读")
    void settlementConfig_propertyAdmin_read() throws Exception {
        mockMvc.perform(get("/admin/settlement-config")
                        .header("Authorization", bearer("res_pa", "property_admin", "pc1", null)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("结算配置: 物业管理员写入 → 403")
    void settlementConfig_propertyAdmin_write_forbidden() throws Exception {
        mockMvc.perform(put("/admin/settlement-config")
                        .header("Authorization", bearer("res_pa", "property_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("结算配置: 平台管理员写入 → 200")
    void settlementConfig_platformAdmin_write() throws Exception {
        mockMvc.perform(put("/admin/settlement-config")
                        .header("Authorization", bearer("res_platform", "platform_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("订单配置: 物业管理员写入 → 403")
    void orderConfig_propertyAdmin_write_forbidden() throws Exception {
        mockMvc.perform(put("/admin/order-config")
                        .header("Authorization", bearer("res_pa", "property_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("订单配置: 平台管理员写入 → 200")
    void orderConfig_platformAdmin_write() throws Exception {
        mockMvc.perform(put("/admin/order-config")
                        .header("Authorization", bearer("res_platform", "platform_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("钱包卡: 住户 → 403")
    void walletCards_resident_forbidden() throws Exception {
        mockMvc.perform(get("/wallet/cards")
                        .param("ownerType", "RESIDENT")
                        .param("ownerId", "res_other")
                        .header("Authorization", bearer("res_1", "resident", "pc1", null)))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("钱包卡: 平台管理员 → 200")
    void walletCards_platformAdmin() throws Exception {
        when(walletCardService.getWalletCards(anyString(), anyString()))
                .thenReturn(new WalletCardsDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false));

        mockMvc.perform(get("/wallet/cards")
                        .param("ownerType", "RESIDENT")
                        .param("ownerId", "res_other")
                        .header("Authorization", bearer("res_platform", "platform_admin", "pc1", null)))
                .andExpect(status().isOk());
    }
}
