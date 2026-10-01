package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证演示商家（mch_demo001 / pc_demo001）的端到端下单链路：
 *  - 修复 GET /products/{id} 匿名访问 99001 NullPointerException
 *  - /cart/delivery-fee、/orders/preview、/orders 接受 mch_ 前缀商家 ID
 *  - /orders/{id}/pay 不再返回 60006（挂接配置存在且 active）
 */
@DisplayName("演示商家下单链路验证")
class DemoMerchantOrderFlowTest extends ControllerTestBase {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MerchantPropertyConfigRepository merchantPropertyConfigRepository;

    @Autowired
    private PropertyCompanyRepository propertyCompanyRepository;

    @Autowired
    private ResidentRepository residentRepository;

    /**
     * 播种演示数据。
     *
     * <p>测试使用跨测试方法共享的 H2 内存库（DB_CLOSE_DELAY=-1），
     * 而本类的 seed 用固定主键（pc_demo001 / prd_seed_cola 等）；
     * 若不先清理，第二个测试方法插入时会命中主键冲突，用例之间互相污染。
     * 因此这里按外键依赖顺序先删除再重建：商品 → 挂接配置 → 商家 → 业主 → 物业公司。
     *
     * <p>注意：清理与播种必须写在同一个 @BeforeEach 方法内 ——
     * JUnit 5 不保证多个 @BeforeEach 方法之间的执行顺序。
     */
    @BeforeEach
    void seed() {
        productRepository.deleteAll();
        merchantPropertyConfigRepository.deleteAll();
        merchantRepository.deleteAll();
        residentRepository.deleteAll();
        propertyCompanyRepository.deleteAll();

        PropertyCompany company = new PropertyCompany();
        company.setId("pc_demo001");
        company.setName("演示物业公司");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        Resident resident = new Resident();
        resident.setId("res_dev001");
        resident.setName("测试业主");
        resident.setPhone("13900000001");
        resident.setPropertyCompanyId("pc_demo001");
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPointBalance(5000);
        resident.setCoinBalance(BigDecimal.ZERO);
        residentRepository.save(resident);

        Merchant merchant = new Merchant();
        merchant.setId("mch_demo001");
        merchant.setPlatformMerchantId("pm_demo001");
        merchant.setPropertyCompanyId("pc_demo001");
        merchant.setName("口福餐厅");
        merchant.setDescription("社区餐厅");
        merchant.setCategory("外卖小吃");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setMerchantSource("platform");
        merchant.setMerchantLevel("property_certified");
        merchant.setDeliveryFee(BigDecimal.ZERO);
        merchant.setFreeDeliverySponsor("merchant");
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantRepository.save(merchant);

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId("mpc_test001");
        config.setMerchantId("mch_demo001");
        config.setPropertyCompanyId("pc_demo001");
        config.setCommissionRate(new BigDecimal("0.1500"));
        config.setPointExchangeRate(new BigDecimal("100.0000"));
        config.setCoinRebateRate(new BigDecimal("0.0200"));
        config.setCoinRebateEnabled(true);
        config.setStatus("active");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        merchantPropertyConfigRepository.save(config);

        Product product = new Product();
        product.setId("prd_seed_cola");
        product.setMerchantId("mch_demo001");
        product.setName("可口可乐330ml*6罐");
        product.setDescription("经典原味碳酸饮料，冰爽畅快");
        product.setPrice(new BigDecimal("18.00"));
        product.setMemberPrice(new BigDecimal("16.00"));
        product.setPointPrice(1800);
        product.setStock(100);
        product.setCategory("饮料");
        product.setStatus("active");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private String residentToken() {
        return bearer("res_dev001", "resident", "pc_demo001", null);
    }

    private String orderBody() {
        return "{\"merchantId\":\"mch_demo001\",\"items\":[{\"productId\":\"prd_seed_cola\",\"quantity\":1}]}";
    }

    @Test
    @DisplayName("GET /products/{id}: 匿名访问返回 code=0（不再 NPE）")
    void anonymousProductDetailShouldSucceed() throws Exception {
        mockMvc.perform(get("/products/prd_seed_cola"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("prd_seed_cola"))
                .andExpect(jsonPath("$.data.merchantId").value("mch_demo001"));
    }

    @Test
    @DisplayName("POST /cart/delivery-fee: merchantId=mch_demo001 返回 code=0")
    void cartDeliveryFeeWithMchId() throws Exception {
        mockMvc.perform(post("/cart/delivery-fee")
                        .header("Authorization", residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("POST /orders/preview: merchantId=mch_demo001 返回 code=0")
    void previewOrderWithMchId() throws Exception {
        mockMvc.perform(post("/orders/preview")
                        .header("Authorization", residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalAmount").isNumber());
    }

    @Test
    @DisplayName("POST /orders + POST /orders/{id}/pay(point): 返回 code=0，不报 60006")
    void createAndPayOrderWithMchId() throws Exception {
        String createBody = mockMvc.perform(post("/orders")
                        .header("Authorization", residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        String orderId = objectMapper.readTree(createBody).path("data").path("orderId").asText();
        org.junit.jupiter.api.Assertions.assertTrue(orderId.startsWith("ord_"));

        String payBody = mockMvc.perform(post("/orders/" + orderId + "/pay")
                        .header("Authorization", residentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"point\"}"))
                .andReturn().getResponse().getContentAsString();
        JsonNode payJson = objectMapper.readTree(payBody);
        org.junit.jupiter.api.Assertions.assertEquals(0, payJson.path("code").asInt(), "payBody=" + payBody);
        org.junit.jupiter.api.Assertions.assertEquals("paid", payJson.path("data").path("orderStatus").asText(), "payBody=" + payBody);
    }
}
