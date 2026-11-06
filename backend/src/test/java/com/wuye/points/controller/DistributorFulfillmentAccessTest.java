package com.wuye.points.controller;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 一级代理商履约权限回归（2026-11-06 订单状态机核对时发现的真实断点）。
 *
 * <p>一级代理载体是审核通过时代建的（{@code AdminDistributorApplicationService}），
 * 申请人的 {@code residents.role} 并不会被改成 {@code merchant}，一直是 {@code resident}；
 * 而代销单的 {@code orders.merchant_id} 就是这个代理载体。此前履约四道门
 * （选择履约方式 / 发布配送 / 置订单状态 / 商家自配确认送达）全部写死
 * {@code requireAnyRole("merchant")}，导致文档 §107.4.1 承诺的
 * 「一级代理商可直接使用既有商家履约接口」实际永远 403。</p>
 *
 * <p>生产实况印证：6 笔在途代销单 {@code fulfillment_mode} 全是 {@code courier_hall}
 * （30 分钟超时兜底默认改派），{@code merchant_self} 为 0 笔 —— 代理根本点不动。</p>
 *
 * <p>修法是把「商家侧操作权」改为认店铺归属（{@code merchants.resident_id}，
 * 全项目唯一口径 {@code MerchantSupport.isOwnedBy}），普通住户名下没有店铺，越不过去。</p>
 */
@DisplayName("一级代理商：履约权限按店铺归属而非角色")
class DistributorFulfillmentAccessTest extends ControllerTestBase {

    private static final String PC = "pc_dst0001";
    private static final String DISTRIBUTOR_USER = "res_dst_owner";
    private static final String OTHER_MERCHANT_USER = "res_dst_other";
    private static final String PLAIN_RESIDENT = "res_dst_plain";
    private static final String DISTRIBUTOR_MERCHANT = "mch_dst_dist";
    private static final String OTHER_MERCHANT = "mch_dst_other";
    private static final String ORDER_ID = "ord_dst0001";

    @Autowired private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private DeliveryRepository deliveryRepository;

    @BeforeEach
    void seed() {
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("一级代理履约测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        // 关键：一级代理的操作账号 role 仍是 resident（与生产 res_dev001 同型）
        resident(DISTRIBUTOR_USER, "13920000001", "resident");
        resident(OTHER_MERCHANT_USER, "13920000002", "merchant");
        resident(PLAIN_RESIDENT, "13920000003", "resident");

        merchant(DISTRIBUTOR_MERCHANT, DISTRIBUTOR_USER, true);
        merchant(OTHER_MERCHANT, OTHER_MERCHANT_USER, false);
        config(DISTRIBUTOR_MERCHANT, "mpc_dst_dist");
        config(OTHER_MERCHANT, "mpc_dst_other");

        orderRepository.save(order());
    }

    @AfterEach
    void cleanUp() {
        deliveryRepository.findByOrderId(ORDER_ID).ifPresent(deliveryRepository::delete);
        orderRepository.deleteById(ORDER_ID);
        merchantPropertyConfigRepository.deleteAllById(List.of("mpc_dst_dist", "mpc_dst_other"));
        merchantRepository.deleteAllById(List.of(DISTRIBUTOR_MERCHANT, OTHER_MERCHANT));
        residentRepository.deleteAllById(List.of(DISTRIBUTOR_USER, OTHER_MERCHANT_USER, PLAIN_RESIDENT));
        propertyCompanyRepository.deleteById(PC);
    }

    private void resident(String id, String phone, String role) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setName("履约测试" + id.substring(4));
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId("com_dst0001");
        resident.setBuilding("1");
        resident.setUnit("1");
        resident.setRoom("101");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        residentRepository.save(resident);
    }

    private void merchant(String id, String ownerResidentId, boolean isDistributor) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setPlatformMerchantId("pmm_" + id);
        merchant.setPropertyCompanyId(PC);
        merchant.setName(isDistributor ? "测试一级代理" : "测试普通商家");
        merchant.setCategory(isDistributor ? "一级代理" : "外卖小吃");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setMerchantSource(isDistributor ? "distributor" : "platform");
        merchant.setIsDistributor(isDistributor);
        merchant.setResidentId(ownerResidentId);
        merchant.setDeliveryFee(BigDecimal.ZERO);
        merchantRepository.save(merchant);
    }

    private void config(String merchantId, String id) {
        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(id);
        config.setMerchantId(merchantId);
        config.setPropertyCompanyId(PC);
        config.setStatus("active");
        config.setCommissionRate(new BigDecimal("0.0500"));
        config.setPointExchangeRate(new BigDecimal("100.0000"));
        merchantPropertyConfigRepository.save(config);
    }

    private Order order() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setOrderNo("NO_DST_0001");
        order.setResidentId(PLAIN_RESIDENT);
        order.setMerchantId(DISTRIBUTOR_MERCHANT);
        order.setPropertyCompanyId(PC);
        order.setOrderStatus("paid");
        order.setTotalAmount(new BigDecimal("11.00"));
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(new BigDecimal("11.00"));
        order.setCouponAmount(BigDecimal.ZERO);
        order.setRequiresDelivery(Boolean.TRUE);
        order.setFulfillmentMode("pending_choice");
        order.setFulfillmentDeadline(LocalDateTime.now().plusMinutes(30));
        order.setSourceType("distributor");
        order.setDistributorId(DISTRIBUTOR_MERCHANT);
        return order;
    }

    @Test
    @DisplayName("一级代理（登录态是 resident）能替自己的代理载体选履约方式（此前恒 403）")
    void distributor_canChooseFulfillment() throws Exception {
        mockMvc.perform(post("/merchants/my/orders/" + ORDER_ID + "/fulfillment")
                        .header("Authorization", bearer(DISTRIBUTOR_USER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"merchant_self\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(ORDER_ID));

        Order order = orderRepository.findById(ORDER_ID).orElseThrow();
        assertEquals("merchant_self", order.getFulfillmentMode());
        assertEquals("delivering", order.getOrderStatus(), "选完履约应进入配送中");
        assertNotNull(deliveryRepository.findByOrderId(ORDER_ID).orElse(null), "应已生成商家自配配送单");
    }

    @Test
    @DisplayName("一级代理能把自己的订单置为已完成并触发结算")
    void distributor_canCompleteOwnOrder() throws Exception {
        Order pending = orderRepository.findById(ORDER_ID).orElseThrow();
        pending.setFulfillmentMode("merchant_self");
        pending.setOrderStatus("delivering");
        orderRepository.save(pending);

        mockMvc.perform(patch("/orders/" + ORDER_ID + "/status")
                        .header("Authorization", bearer(DISTRIBUTOR_USER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderStatus\":\"completed\"}"))
                .andExpect(status().isOk());

        assertEquals("completed", orderRepository.findById(ORDER_ID).orElseThrow().getOrderStatus());
    }

    @Test
    @DisplayName("普通住户名下没有店铺，仍然进不了商家履约接口")
    void plainResident_stillForbidden() throws Exception {
        mockMvc.perform(post("/merchants/my/orders/" + ORDER_ID + "/fulfillment")
                        .header("Authorization", bearer(PLAIN_RESIDENT, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"merchant_self\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("放宽只放到「名下有店铺」，别的商家依然不能操作这张单")
    void otherMerchant_stillForbidden() throws Exception {
        mockMvc.perform(post("/merchants/my/orders/" + ORDER_ID + "/fulfillment")
                        .header("Authorization", bearer(OTHER_MERCHANT_USER, "merchant", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"merchant_self\"}"))
                .andExpect(status().isForbidden());
    }
}
