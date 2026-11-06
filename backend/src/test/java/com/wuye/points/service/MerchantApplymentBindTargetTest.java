package com.wuye.points.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 「进件成功默认零钱」绑定对象解析测试：主体住户优先，绝不绑到代办运营账号。
 */
class MerchantApplymentBindTargetTest {

    @Test
    @DisplayName("申请单主体商户有 resident_id：优先用主体住户，不用登录操作人")
    void merchantResidentWinsOverOperator() {
        MerchantApplymentService.BindTarget target = MerchantApplymentService.resolveBindTarget(
                "res_merchant", "pc_001", "res_operator", "pc_001");
        assertEquals("res_merchant", target.residentId());
        assertEquals("pc_001", target.propertyCompanyId());
    }

    @Test
    @DisplayName("主体缺 resident_id：回退到发起进件的登录住户")
    void fallsBackToLoginResident() {
        MerchantApplymentService.BindTarget target = MerchantApplymentService.resolveBindTarget(
                null, null, "res_login", "pc_002");
        assertEquals("res_login", target.residentId());
        assertEquals("pc_002", target.propertyCompanyId());
    }

    @Test
    @DisplayName("主体与登录态都取不到：返回 null（记待绑定，不瞎猜）")
    void neither_returnsNull() {
        assertNull(MerchantApplymentService.resolveBindTarget("", null, null, null));
    }
}
