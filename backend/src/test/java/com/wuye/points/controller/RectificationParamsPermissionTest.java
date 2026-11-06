package com.wuye.points.controller;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPointPurchase;
import com.wuye.points.entity.PointPoolRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantPointPurchaseRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PointPoolRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentForumBanRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 整改清单（后端）参数校验、权限与资金类问题的回归测试。
 *
 * <p>覆盖条目 17/18/21/22/23/24/25/26/30/33。真实 Spring 上下文 + H2 + 真实拦截器链，
 * 断言的是「线上表现」而不是方法内部实现，避免改了代码但线上仍然报错。</p>
 */
@DisplayName("整改清单回归：参数/权限/资金")
class RectificationParamsPermissionTest extends ControllerTestBase {

    private static final String PC = "pc_rect0001";
    private static final String MERCHANT_OWNER = "res_rect_owner";
    private static final String BUYER = "res_rect_buyer";
    private static final String ADMIN = "res_rect_admin";
    private static final String PLATFORM_ADMIN = "res_rect_platform";
    private static final String MERCHANT_ID = "mch_rect0001";

    @Autowired
    private ResidentRepository residentRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantPointPurchaseRepository purchaseRepository;
    @Autowired
    private PointPoolRecordRepository pointPoolRecordRepository;
    @Autowired
    private ResidentForumBanRepository forumBanRepository;
    @Autowired
    private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired
    private com.wuye.points.repository.ProductRepository productRepository;
    @Autowired
    private com.wuye.points.repository.OrderRepository orderRepository;
    @Autowired
    private com.wuye.points.repository.OrderItemRepository orderItemRepository;
    @Autowired
    private com.wuye.points.repository.UserCouponRepository userCouponRepository;
    @Autowired
    private com.wuye.points.service.CouponService couponService;
    @Autowired
    private com.wuye.points.repository.BuildingLeaderRepository buildingLeaderRepository;
    @Autowired
    private com.wuye.points.repository.MerchantPropertyConfigRepository merchantPropertyConfigRepository;

    /** 商家在该物业未完成挂接时下单/试算会报 60006，测试需要补一条挂接配置。 */
    private void ensureMerchantConfig() {
        if (merchantPropertyConfigRepository.existsById("mpc_rect0001")) {
            return;
        }
        com.wuye.points.entity.MerchantPropertyConfig config =
                new com.wuye.points.entity.MerchantPropertyConfig();
        config.setId("mpc_rect0001");
        config.setMerchantId(MERCHANT_ID);
        config.setPropertyCompanyId(PC);
        config.setCommissionRate(new BigDecimal("0.0500"));
        config.setPointExchangeRate(new BigDecimal("100.0000"));
        config.setStatus("active");
        merchantPropertyConfigRepository.save(config);
    }

    /** 管理端接口普遍要求物业公司存在（resolveRequiredPropertyCompanyId 会 existsById 校验）。 */
    private void ensurePropertyCompany() {
        if (propertyCompanyRepository.existsById(PC)) {
            return;
        }
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("整改测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);
    }

    private Resident resident(String id, String phone, String role) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setName("整改测试" + id.substring(4));
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId("com_rect0001");
        resident.setBuilding("1");
        resident.setUnit("1");
        resident.setRoom(id.equals(BUYER) ? "101" : "102");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        return residentRepository.save(resident);
    }

    private Merchant merchant(boolean withdrawalBlocked) {
        Merchant merchant = new Merchant();
        merchant.setId(MERCHANT_ID);
        merchant.setPlatformMerchantId("pmm_" + MERCHANT_ID);
        merchant.setPropertyCompanyId(PC);
        merchant.setName("整改测试店");
        merchant.setCategory("外卖小吃");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setMerchantSource("merchant");
        merchant.setContactPhone("13910000001");
        merchant.setResidentId(MERCHANT_OWNER);
        merchant.setDeliveryFee(new BigDecimal("3.00"));
        merchant.setWithdrawalBlocked(withdrawalBlocked);
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        return merchantRepository.save(merchant);
    }

    @AfterEach
    void cleanUp() {
        forumBanRepository.deleteAllById(java.util.List.of("rfb_rect0001"));
        purchaseRepository.deleteAllById(java.util.List.of("mpp_rect0001"));
        pointPoolRecordRepository.deleteAllById(java.util.List.of("ppr_rect0001"));
        userCouponRepository.deleteAllById(java.util.List.of("ucp_rect0001"));
        orderItemRepository.deleteAllById(java.util.List.of("oit_rect0001"));
        orderRepository.deleteById("ord_rect0001");
        productRepository.deleteById("prd_rect0001");
        buildingLeaderRepository.deleteAll(buildingLeaderRepository.findByResidentId(BUYER));
        merchantPropertyConfigRepository.deleteById("mpc_rect0001");
        merchantRepository.deleteById(MERCHANT_ID);
        residentRepository.deleteAllById(java.util.List.of(MERCHANT_OWNER, BUYER, ADMIN, PLATFORM_ADMIN));
        propertyCompanyRepository.deleteById(PC);
    }

    // ==================== 条目24/25：管理端接口不得被住户/商家令牌穿透 ====================

    @Test
    @DisplayName("条目24：纯住户令牌调 /admin/ 已登记权限码的接口 → 403")
    void residentToken_forbiddenOnAdminRoutes() throws Exception {
        resident(BUYER, "13910000201", "resident");

        mockMvc.perform(get("/admin/residents")
                        .param("propertyCompanyId", PC)
                        .header("Authorization", bearer(BUYER, "resident", PC, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("条目24：/admin/ 未登记权限码的路由（价格审批）也被工作台身份兜底挡住 → 403")
    void residentToken_forbiddenOnUnmappedAdminRoute() throws Exception {
        resident(BUYER, "13910000201", "resident");

        mockMvc.perform(get("/admin/price-approvals")
                        .header("Authorization", bearer(BUYER, "resident", PC, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("条目25：商家令牌调 /admin/ 接口 → 403")
    void merchantToken_forbiddenOnAdminRoutes() throws Exception {
        resident(MERCHANT_OWNER, "13910000001", "merchant");
        merchant(false);

        mockMvc.perform(get("/admin/merchants")
                        .param("propertyCompanyId", PC)
                        .header("Authorization", bearer(MERCHANT_OWNER, "merchant", PC, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("条目24：纯住户用手机号密码登录管理端 → 403，且不受测试模式影响")
    void pureResident_cannotGetAdminToken() throws Exception {
        Resident plain = resident(BUYER, "13910000201", "resident");
        plain.setPassword(org.springframework.security.crypto.bcrypt.BCrypt.hashpw("Admin@12345",
                org.springframework.security.crypto.bcrypt.BCrypt.gensalt()));
        residentRepository.save(plain);

        mockMvc.perform(post("/auth/admin-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13910000201\",\"password\":\"Admin@12345\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("该账号无管理端登录权限"));
    }

    // ==================== 条目33/30：缺参不再 500 ====================

    @Test
    @DisplayName("条目33：解绑收款账号不带 channel → 400 参数缺失（此前 500 内部服务异常）")
    void unbindWithoutChannel_isBadRequest() throws Exception {
        resident(ADMIN, "13910000301", "property_admin");

        mockMvc.perform(delete("/transfer-accounts/my")
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("channel")));
    }

    @Test
    @DisplayName("条目33：channel 取非法值 → 400（不再静默成功）")
    void unbindWithIllegalChannel_isBadRequest() throws Exception {
        resident(ADMIN, "13910000301", "property_admin");

        mockMvc.perform(delete("/transfer-accounts/my")
                        .param("channel", "wechat")
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10002));
    }

    @Test
    @DisplayName("条目30：公开货源列表不带 propertyCompanyId → 按登录态物业返回 200（此前 500）")
    void publicListingsWithoutPropertyScope_fallsBackToLoginContext() throws Exception {
        resident(BUYER, "13910000201", "resident");

        mockMvc.perform(get("/resident-merchants/public-listings")
                        .header("Authorization", bearer(BUYER, "resident", PC, null)))
                .andExpect(status().isOk());
    }

    // ==================== 条目22：积分池记录不再 NPE ====================

    @Test
    @DisplayName("条目22：operator_id 为空的积分池流水列表 → 200（此前 Map.of().get(null) 抛 NPE 500）")
    void pointPoolRecordsWithNullOperator_returnOk() throws Exception {
        ensurePropertyCompany();
        resident(ADMIN, "13910000301", "property_admin");
        PointPoolRecord record = new PointPoolRecord();
        record.setId("ppr_rect0001");
        record.setPropertyCompanyId(PC);
        record.setPoolType("community");
        record.setAmount(BigDecimal.ONE);
        record.setBalance(BigDecimal.ONE);
        record.setSource("manual");
        record.setDescription("整改测试：无操作人");
        record.setOperatorId(null);
        pointPoolRecordRepository.save(record);

        mockMvc.perform(get("/admin/point-pools/records")
                        .param("propertyCompanyId", PC)
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id == 'ppr_rect0001')]",
                        org.hamcrest.Matchers.hasSize(1)));
    }

    // ==================== 条目13：取消支付后重进支付页 ====================

    @Test
    @DisplayName("条目13：本人待支付订单已预占的库存，试算时豁免（此前误报库存不足→前端「暂无法计算优惠」）")
    void preview_exemptsOwnPendingOrderReservedStock() throws Exception {
        resident(BUYER, "13910000201", "resident");
        merchant(false);
        ensureMerchantConfig();

        com.wuye.points.entity.Product product = new com.wuye.points.entity.Product();
        product.setId("prd_rect0001");
        product.setMerchantId(MERCHANT_ID);
        product.setName("整改测试末件商品");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(0); // 唯一一件已被本人待支付订单预占
        product.setStatus("active");
        productRepository.save(product);

        com.wuye.points.entity.Order order = new com.wuye.points.entity.Order();
        order.setId("ord_rect0001");
        order.setOrderNo("NO_RECT_0001");
        order.setResidentId(BUYER);
        order.setMerchantId(MERCHANT_ID);
        order.setPropertyCompanyId(PC);
        order.setOrderStatus("pending");
        order.setTotalAmount(new BigDecimal("10.00"));
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        orderRepository.save(order);

        com.wuye.points.entity.OrderItem item = new com.wuye.points.entity.OrderItem();
        item.setId("oit_rect0001");
        item.setOrderId("ord_rect0001");
        item.setProductId("prd_rect0001");
        item.setProductName("整改测试末件商品");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setTotalPrice(new BigDecimal("10.00"));
        orderItemRepository.save(item);

        mockMvc.perform(post("/orders/preview")
                        .header("Authorization", bearer(BUYER, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantId\":\"" + MERCHANT_ID + "\","
                                + "\"items\":[{\"productId\":\"prd_rect0001\",\"quantity\":1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productAmount").value(
                        org.hamcrest.Matchers.closeTo(10.0, 0.001)));
    }

    @Test
    @DisplayName("条目13：业主上架（货源）单详情回 listingId/sourceType，前端据此改调 from-listing/preview")
    void orderDetail_exposesListingIdForSourcedOrder() throws Exception {
        resident(BUYER, "13910000201", "resident");
        merchant(false);

        com.wuye.points.entity.Product product = new com.wuye.points.entity.Product();
        product.setId("prd_rect0001");
        product.setMerchantId(MERCHANT_ID);
        product.setName("整改测试货源占位");
        product.setPrice(new BigDecimal("11.00"));
        product.setStock(1);
        product.setStatus("active");
        productRepository.save(product);

        com.wuye.points.entity.Order order = new com.wuye.points.entity.Order();
        order.setId("ord_rect0001");
        order.setOrderNo("NO_RECT_0001");
        order.setResidentId(BUYER);
        order.setMerchantId(MERCHANT_ID);
        order.setPropertyCompanyId(PC);
        order.setOrderStatus("pending");
        order.setTotalAmount(new BigDecimal("11.00"));
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setSourceType("distributor");
        order.setListingId("rml_rect0001");
        orderRepository.save(order);

        com.wuye.points.entity.OrderItem item = new com.wuye.points.entity.OrderItem();
        item.setId("oit_rect0001");
        item.setOrderId("ord_rect0001");
        item.setProductId("dpr_rect0001");
        item.setProductName("整改测试货源商品");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("11.00"));
        item.setTotalPrice(new BigDecimal("11.00"));
        orderItemRepository.save(item);

        // 支付页要按这两个字段决定试算走哪个接口：详情不给 listingId，前端只能拿 dpr_ 去调
        // /orders/preview，后端查不到商城商品 → 「货源商品请经由业主上架商品下单」。
        mockMvc.perform(get("/orders/ord_rect0001")
                        .header("Authorization", bearer(BUYER, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.listingId").value("rml_rect0001"))
                .andExpect(jsonPath("$.data.sourceType").value("distributor"))
                .andExpect(jsonPath("$.data.items[0].productId").value("dpr_rect0001"));
    }

    @Test
    @DisplayName("条目13：同一订单重复发起支付，已为本单锁定的券不再被判「券不可用」")
    void lockCoupon_alreadyLockedBySameOrder_isAccepted() {
        resident(BUYER, "13910000201", "resident");
        merchant(false);

        com.wuye.points.entity.UserCoupon coupon = new com.wuye.points.entity.UserCoupon();
        coupon.setId("ucp_rect0001");
        coupon.setTemplateId("tpl_rect0001");
        coupon.setResidentId(BUYER);
        coupon.setMerchantId(MERCHANT_ID);
        coupon.setPropertyCompanyId(PC);
        coupon.setStatus("locked");
        coupon.setSource("manual");
        coupon.setName("整改测试券");
        coupon.setType("amount");
        coupon.setValueAmount(new BigDecimal("2.00"));
        coupon.setMinSpendAmount(new BigDecimal("5.00"));
        coupon.setExpireAt(LocalDateTime.now().plusDays(7));
        coupon.setOrderId("ord_rect0001");
        userCouponRepository.save(coupon);

        java.math.BigDecimal discount = couponService.lockForOrder("ucp_rect0001", BUYER,
                MERCHANT_ID, new BigDecimal("10.00"), "ord_rect0001");

        org.junit.jupiter.api.Assertions.assertEquals(0,
                new BigDecimal("2.00").compareTo(discount), "同一订单重复支付应沿用该券抵扣额");
    }

    // ==================== 条目17/18：商家积分购买与发放 ====================

    @Test
    @DisplayName("条目17：购买积分只传 pointAmount → 200，应付金额由后端按兑换比折算")
    void purchasePoints_withoutPayAmount_ok() throws Exception {
        resident(MERCHANT_OWNER, "13910000001", "merchant");
        merchant(false);

        mockMvc.perform(post("/merchant/points/purchase")
                        .header("Authorization", bearer(MERCHANT_OWNER, "merchant", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pointAmount\":1000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pointAmount").value(1000))
                // 默认兑换比 100 积分 = 1 元 → 10.00（JSON 数字按 double 比较）
                .andExpect(jsonPath("$.data.payAmount").value(org.hamcrest.Matchers.closeTo(10.0, 0.001)))
                .andExpect(jsonPath("$.data.exchangeRate").exists());
    }

    @Test
    @DisplayName("条目18：发放积分按手机号选人、无需 purchaseId → 200，住户到账")
    void grantPoints_byPhone_ok() throws Exception {
        resident(MERCHANT_OWNER, "13910000001", "merchant");
        resident(BUYER, "13910000201", "resident");
        merchant(false);

        MerchantPointPurchase purchase = new MerchantPointPurchase();
        purchase.setId("mpp_rect0001");
        purchase.setMerchantId(MERCHANT_ID);
        purchase.setPropertyCompanyId(PC);
        purchase.setPointAmount(500);
        purchase.setPayAmount(new BigDecimal("5.00"));
        purchase.setAuditStatus("approved");
        purchase.setRemainingPoints(500);
        purchaseRepository.save(purchase);

        mockMvc.perform(post("/merchant/points/grant")
                        .header("Authorization", bearer(MERCHANT_OWNER, "merchant", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13910000201\",\"pointAmount\":100,\"description\":\"消费赠送\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.residentId").value(BUYER))
                .andExpect(jsonPath("$.data.purchaseRemainingPoints").value(400));

        org.junit.jupiter.api.Assertions.assertEquals(100,
                residentRepository.findById(BUYER).orElseThrow().getPointBalance());
    }

    // ==================== 条目23：封禁商家不能提现 ====================

    @Test
    @DisplayName("条目23：商家物业币提现被封禁后发起兑换 → 94005")
    void blockedMerchant_cannotApplyCoinWithdrawal() throws Exception {
        resident(MERCHANT_OWNER, "13910000001", "merchant");
        merchant(true);

        mockMvc.perform(post("/coin-withdrawals")
                        .header("Authorization", bearer(MERCHANT_OWNER, "merchant", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coinAmount\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(94005));
    }

    // ==================== 条目21：任命楼长支持手机号 ====================

    @Test
    @DisplayName("条目21：物业经理用手机号任命楼长 → 200（不再强迫前端持有住户ID）")
    void appointBuildingLeader_byPhone_ok() throws Exception {
        resident(ADMIN, "13910000301", "property_admin");
        resident(BUYER, "13910000201", "resident");

        mockMvc.perform(post("/admin/building-leaders")
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13910000201\",\"commissionRate\":0.1000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.residentId").value(BUYER));
    }

    // ==================== 条目26：禁言到期时间格式 ====================

    @Test
    @DisplayName("条目26：禁言到期时间格式不支持时返回可自助修复的 400 文案（含支持的格式清单）")
    void forumBan_unsupportedExpiryFormat_isBadRequest() throws Exception {
        resident(ADMIN, "13910000301", "property_admin");
        resident(BUYER, "13910000201", "resident");

        mockMvc.perform(post("/admin/community/users/" + BUYER + "/forum-ban")
                        .header("Authorization", bearer(ADMIN, "property_admin", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"整改测试\",\"expiresAt\":\"10/11/2026 14:30\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10002))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("expiresAt")));
    }
}
