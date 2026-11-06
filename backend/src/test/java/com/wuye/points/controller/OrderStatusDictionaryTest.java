package com.wuye.points.controller;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 订单状态字典与状态机一致性回归（2026-11-06）。
 *
 * <p>状态机里真实存在 {@code pending_verification}（团购单支付后待商家核实）与
 * {@code verified}（核实通过、冻结期中）两个状态，且 {@code requestRefund} 明确允许这两个状态申请退货，
 * 但它们一直不在 {@code RoleGuard.ORDER_STATUS} 白名单里：管理端筛选器（enums.ts 的
 * {@code ORDER_STATUS_OPTIONS}）把这两项列成了可选项，选中后查列表会被判
 * {@code 400 orderStatus值无效}。本类把「可查」与「不可写」两头钉住。</p>
 */
@DisplayName("订单状态字典：可筛选、不可乱写")
class OrderStatusDictionaryTest extends ControllerTestBase {

    private static final String PC = "pc_osd0001";
    private static final String BUYER = "res_osd_buyer";
    private static final String ADMIN = "res_osd_admin";
    private static final String MERCHANT_OWNER = "res_osd_owner";
    private static final String MERCHANT = "mch_osd0001";
    private static final String ORDER_ID = "ord_osd0001";

    @Autowired private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private OrderRepository orderRepository;

    @BeforeEach
    void seed() {
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("状态字典测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        resident(BUYER, "13930000001", "resident");
        resident(ADMIN, "13930000002", "property_admin");
        resident(MERCHANT_OWNER, "13930000003", "merchant");

        Merchant merchant = new Merchant();
        merchant.setId(MERCHANT);
        merchant.setPlatformMerchantId("pmm_" + MERCHANT);
        merchant.setPropertyCompanyId(PC);
        merchant.setName("状态字典测试店");
        merchant.setCategory("团购");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setMerchantSource("platform");
        merchant.setResidentId(MERCHANT_OWNER);
        merchant.setDeliveryFee(BigDecimal.ZERO);
        merchantRepository.save(merchant);

        Order order = new Order();
        order.setId(ORDER_ID);
        order.setOrderNo("NO_OSD_0001");
        order.setResidentId(BUYER);
        order.setMerchantId(MERCHANT);
        order.setPropertyCompanyId(PC);
        order.setOrderStatus("pending_verification");
        order.setTotalAmount(new BigDecimal("10.00"));
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        orderRepository.save(order);
    }

    @AfterEach
    void cleanUp() {
        orderRepository.deleteById(ORDER_ID);
        merchantRepository.deleteById(MERCHANT);
        residentRepository.deleteAllById(List.of(BUYER, ADMIN, MERCHANT_OWNER));
        propertyCompanyRepository.deleteById(PC);
    }

    private void resident(String id, String phone, String role) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setName("状态字典" + id.substring(4));
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId("com_osd0001");
        resident.setBuilding("1");
        resident.setUnit("1");
        resident.setRoom("101");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        residentRepository.save(resident);
    }

    @Test
    @DisplayName("买家按 pending_verification 筛选自己的订单 → 200（此前 400）")
    void buyer_canFilterByPendingVerification() throws Exception {
        mockMvc.perform(get("/orders/my")
                        .param("orderStatus", "pending_verification")
                        .header("Authorization", bearer(BUYER, "resident", PC, null)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("管理端按 verified / pending_verification 筛选 → 200（筛选器里本就有这两项）")
    void admin_canFilterVerificationStates() throws Exception {
        for (String statusValue : List.of("verified", "pending_verification")) {
            mockMvc.perform(get("/orders")
                            .param("orderStatus", statusValue)
                            .param("propertyCompanyId", PC)
                            .header("Authorization", bearer(ADMIN, "property_admin", PC, null)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("字典放开不等于能写：商家把订单置成 verified 仍被状态机拒绝")
    void merchant_cannotWriteVerifiedStatus() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/orders/" + ORDER_ID + "/status")
                        .header("Authorization", bearer(MERCHANT_OWNER, "merchant", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderStatus\":\"verified\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("非法状态值仍然被挡，白名单没有变成来者不拒")
    void bogusStatusStillRejected() throws Exception {
        mockMvc.perform(get("/orders/my")
                        .param("orderStatus", "not_a_status")
                        .header("Authorization", bearer(BUYER, "resident", PC, null)))
                .andExpect(status().isBadRequest());
    }
}
