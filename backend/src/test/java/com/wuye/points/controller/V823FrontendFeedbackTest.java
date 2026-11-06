package com.wuye.points.controller;

import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.DistributorProduct;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantAd;
import com.wuye.points.entity.Message;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.OrderItem;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.DistributorProductRepository;
import com.wuye.points.repository.MerchantAdRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.OrderItemRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * v8.23 前端联调反馈（9 项）回归验证。
 *
 * <p>覆盖：商家余额口径、广告补付/枚举、积分转物业开关与资格、会话楼长标识、
 * 商品店铺分流字段、货源供给范围权限、代理购买记录筛选/脱敏、历史转账流水筛选。</p>
 */
@DisplayName("v8.23 前端联调反馈修复验证")
class V823FrontendFeedbackTest extends ControllerTestBase {

    private static final String PC = "pc_v823";
    private static final String VIEWER = "res_v823_viewer";
    private static final String VIEWER_PHONE = "13900000001";
    private static final String LEADER = "res_v823_leader";
    private static final String LEADER_PHONE = "13900000002";
    private static final String BUYER = "res_v823_buyer";
    private static final String BUYER_PHONE = "13800138000";
    private static final String MERCHANT_USER = "res_v823_merchant";
    private static final String MERCHANT_PHONE = "13900000009";
    private static final String DIST_USER = "res_v823_dist";
    private static final String DIST_PHONE = "13900000007";
    private static final String RECENT = "res_v823_recent";
    private static final String RECENT_PHONE = "13900000003";

    private static final String SHOP_ID = "mch_v823_shop";
    private static final String RESIDENT_SHOP_ID = "mch_v823_resident_shop";
    private static final String DIST_ID = "mch_v823_dist";

    @Autowired
    private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired
    private ResidentRepository residentRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DistributorProductRepository distributorProductRepository;
    @Autowired
    private BuildingLeaderRepository buildingLeaderRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MerchantAdRepository merchantAdRepository;
    @Autowired
    private PaymentRecordRepository paymentRecordRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private PointRecordRepository pointRecordRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seed() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        paymentRecordRepository.deleteAll();
        merchantAdRepository.deleteAll();
        distributorProductRepository.deleteAll();
        productRepository.deleteAll();
        merchantPropertyConfigRepository.deleteAll();
        merchantRepository.deleteAll();
        buildingLeaderRepository.deleteAll();
        messageRepository.deleteAll();
        pointRecordRepository.deleteAll();
        residentRepository.deleteAll();
        propertyCompanyRepository.deleteAll();

        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("v8.23 演示物业");
        company.setStatus("active");
        company.setPointTransferEnabled(false);
        company.setPointTransferMinYears(5);
        propertyCompanyRepository.save(company);

        saveResident(VIEWER, "resident", VIEWER_PHONE, "王五", 6);
        saveResident(LEADER, "resident", LEADER_PHONE, "李四", 6);
        saveResident(BUYER, "resident", BUYER_PHONE, "张三", 6);
        saveResident(MERCHANT_USER, "merchant", MERCHANT_PHONE, "超市老板", 6);
        saveResident(DIST_USER, "merchant", DIST_PHONE, "一级代理", 6);
        saveResident(RECENT, "resident", RECENT_PHONE, "新住户", 0);

        saveMerchant(SHOP_ID, "便民超市", "platform", MERCHANT_PHONE, MERCHANT_USER);
        Merchant residentShop = saveMerchant(RESIDENT_SHOP_ID, "张阿姨的小店",
                "resident_shop", BUYER_PHONE, BUYER);
        residentShop.setIsRecommended(true);
        residentShop.setRecommendedSort(100);
        merchantRepository.save(residentShop);
        Merchant distributor = saveMerchant(DIST_ID, "精选货源", "platform", DIST_PHONE, DIST_USER);
        distributor.setIsDistributor(true);
        distributor.setMerchantLevel("official_certified");
        merchantRepository.save(distributor);

        saveProduct("prd_v823_normal", SHOP_ID, "土豆1斤", new BigDecimal("3.00"), 7);
        saveProduct("prd_v823_resident", RESIDENT_SHOP_ID, "手工饼干", new BigDecimal("20.00"), 1);

        BuildingLeader leader = new BuildingLeader();
        leader.setId("bl_v823");
        leader.setResidentId(LEADER);
        leader.setPropertyCompanyId(PC);
        leader.setCommunityId("com_v823");
        leader.setUnit("1单元");
        leader.setCommissionRate(new BigDecimal("0.1000"));
        leader.setStatus("active");
        buildingLeaderRepository.save(leader);

        Message message = new Message();
        message.setId("msg_v823_1");
        message.setFromId(LEADER);
        message.setToId(VIEWER);
        message.setMessageType("text");
        message.setContent("邻居你好");
        message.setChatType("private");
        message.setReadStatus("unread");
        message.setPropertyCompanyId(PC);
        message.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message);

        MerchantAd endedAd = new MerchantAd();
        endedAd.setId("mad_v823_ended");
        endedAd.setMerchantId(SHOP_ID);
        endedAd.setTitle("已结束广告");
        endedAd.setContent("历史投放");
        endedAd.setWeekStartDate(LocalDate.now().minusWeeks(2));
        endedAd.setDurationDays(7);
        endedAd.setStartDate(LocalDate.now().minusDays(10));
        endedAd.setEndDate(LocalDate.now().minusDays(2));
        endedAd.setPayAmount(BigDecimal.ZERO);
        endedAd.setPaymentMethod("free");
        endedAd.setStatus("active");
        endedAd.setCreatedAt(LocalDateTime.now().minusDays(10));
        merchantAdRepository.save(endedAd);

        MerchantAd pendingAd = new MerchantAd();
        pendingAd.setId("mad_v823_pending");
        pendingAd.setMerchantId(SHOP_ID);
        pendingAd.setTitle("待支付广告");
        pendingAd.setContent("等待付款");
        pendingAd.setWeekStartDate(LocalDate.now());
        pendingAd.setDurationDays(7);
        pendingAd.setPayAmount(new BigDecimal("70.00"));
        pendingAd.setPaymentMethod("wechat");
        pendingAd.setStatus("pending_payment");
        pendingAd.setCreatedAt(LocalDateTime.now());
        merchantAdRepository.save(pendingAd);

        PaymentRecord pendingPayment = new PaymentRecord();
        pendingPayment.setId("pay_v823_pending");
        pendingPayment.setBizType(PaymentBizType.AD_PURCHASE);
        pendingPayment.setBizId(pendingAd.getId());
        pendingPayment.setResidentId(MERCHANT_USER);
        pendingPayment.setPropertyCompanyId(PC);
        pendingPayment.setPaymentMethod("wechat");
        pendingPayment.setAmount(new BigDecimal("70.00"));
        pendingPayment.setCashAmount(new BigDecimal("70.00"));
        pendingPayment.setStatus("pending");
        pendingPayment.setOutTradeNo("otn_v823_ad_1");
        paymentRecordRepository.save(pendingPayment);

        DistributorProduct reservoir = new DistributorProduct();
        reservoir.setId("dpr_v823_1");
        reservoir.setDistributorMerchantId(DIST_ID);
        reservoir.setName("五常大米 5kg");
        reservoir.setWholesalePrice(new BigDecimal("30.00"));
        reservoir.setSuggestedRetailPrice(new BigDecimal("39.90"));
        reservoir.setStock(50);
        reservoir.setStatus("active");
        reservoir.setSupplyScope("company");
        distributorProductRepository.save(reservoir);

        saveOrder("ord_v823_1", "NO-V823-1", "五常大米 5kg", new BigDecimal("39.90"),
                LocalDateTime.now().minusDays(1), "幸福小区3栋2单元501");
        saveOrder("ord_v823_2", "NO-V823-2", "鲁花花生油 5L", new BigDecimal("109.00"),
                LocalDateTime.now().minusDays(10), "幸福小区3栋2单元502");

        PointRecord familyTransfer = new PointRecord();
        familyTransfer.setId("pt_v823_transfer");
        familyTransfer.setResidentId(VIEWER);
        familyTransfer.setPropertyCompanyId(PC);
        familyTransfer.setPointType("transfer");
        familyTransfer.setAmount(-100);
        familyTransfer.setBalance(4900);
        familyTransfer.setSource("transfer");
        familyTransfer.setDescription("家庭内积分转移");
        familyTransfer.setCreatedAt(LocalDateTime.now().minusDays(1));
        pointRecordRepository.save(familyTransfer);

        PointRecord legacyTransfer = new PointRecord();
        legacyTransfer.setId("pt_v823_family_transfer");
        legacyTransfer.setResidentId(VIEWER);
        legacyTransfer.setPropertyCompanyId(PC);
        legacyTransfer.setPointType("transfer");
        legacyTransfer.setAmount(100);
        legacyTransfer.setBalance(5000);
        legacyTransfer.setSource("family_transfer");
        legacyTransfer.setDescription("早期家庭内积分转移");
        legacyTransfer.setCreatedAt(LocalDateTime.now());
        pointRecordRepository.save(legacyTransfer);
    }

    // ==================== 2.1 商家余额口径 ====================

    @Test
    @DisplayName("2.1 GET /merchants/my：profitBalance 为店铺口径，另返回账号统一可提现")
    void merchantBalance_shouldExposeShopAndAccountScope() throws Exception {
        mockMvc.perform(get("/merchants/my")
                        .header("Authorization", token(MERCHANT_USER, "merchant")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profitScope").value("shop"))
                .andExpect(jsonPath("$.data.profitBalance").exists())
                .andExpect(jsonPath("$.data.shopWithdrawableAmount").exists())
                .andExpect(jsonPath("$.data.accountWithdrawableAmount").exists())
                .andExpect(jsonPath("$.data.withdrawableAmount").exists());
    }

    // ==================== 2.2 / 2.4 广告 ====================

    @Test
    @DisplayName("2.4 GET /merchant/ads：返回 ended/pending_payment、payStatus、paymentRecordId、canRepay")
    void adList_shouldExposeFullStatusEnum() throws Exception {
        mockMvc.perform(get("/merchant/ads")
                        .header("Authorization", token(MERCHANT_USER, "merchant")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id == 'mad_v823_ended')].status", hasItem("ended")))
                .andExpect(jsonPath("$.data.list[?(@.id == 'mad_v823_ended')].payStatus", hasItem("paid")))
                .andExpect(jsonPath("$.data.list[?(@.id == 'mad_v823_ended')].remainingDays", hasItem(0)))
                .andExpect(jsonPath("$.data.list[?(@.id == 'mad_v823_ended')].canRepay", hasItem(false)))
                .andExpect(jsonPath("$.data.list[?(@.id == 'mad_v823_pending')].status",
                        hasItem("pending_payment")))
                .andExpect(jsonPath("$.data.list[?(@.id == 'mad_v823_pending')].payStatus",
                        hasItem("unpaid")))
                .andExpect(jsonPath("$.data.list[?(@.id == 'mad_v823_pending')].canRepay",
                        hasItem(true)))
                .andExpect(jsonPath("$.data.list[?(@.id == 'mad_v823_pending')].paymentRecordId",
                        hasItem("pay_v823_pending")));
    }

    @Test
    @DisplayName("2.2 POST /merchant/ads/{id}/repay：校验归属，微信通道未配置时明确报错")
    void adRepay_shouldValidateOwnershipAndSwitch() throws Exception {
        mockMvc.perform(post("/merchant/ads/mad_v823_pending/repay")
                        .header("Authorization", token(MERCHANT_USER, "merchant")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("在线支付未开通")));

        mockMvc.perform(post("/merchant/ads/mad_v823_pending/repay")
                        .header("Authorization", token(DIST_USER, "merchant")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/merchant/ads/mad_v823_ended/repay")
                        .header("Authorization", token(MERCHANT_USER, "merchant")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("无需支付")));
    }

    // ==================== 2.3 积分转物业开关 ====================

    @Test
    @DisplayName("2.3 GET /property-companies/current：返回 pointTransferEnabled / pointTransferMinYears")
    void propertyCompanyCurrent_shouldExposePointTransferFields() throws Exception {
        mockMvc.perform(get("/property-companies/current")
                        .header("Authorization", token(VIEWER, "resident")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pointTransferEnabled").value(false))
                .andExpect(jsonPath("$.data.pointTransferMinYears").value(5));
    }

    @Test
    @DisplayName("2.3 管理端可开启开关；积分余额接口返回是否满足入住年限")
    void pointTransferEligibility_shouldFollowAdminSwitchAndTenure() throws Exception {
        mockMvc.perform(put("/admin/property-companies/" + PC)
                        .header("Authorization", token("res_v823_admin", "property_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pointTransferEnabled\":true,\"pointTransferMinYears\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pointTransferEnabled").value(true))
                .andExpect(jsonPath("$.data.pointTransferMinYears").value(2));

        mockMvc.perform(get("/points/balance")
                        .header("Authorization", token(VIEWER, "resident")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pointTransferEnabled").value(true))
                .andExpect(jsonPath("$.data.pointTransferMinYears").value(2))
                .andExpect(jsonPath("$.data.pointTransferEligible").value(true))
                .andExpect(jsonPath("$.data.pointTransferBlockedReason").doesNotExist());

        mockMvc.perform(get("/residents/me/points")
                        .header("Authorization", token(RECENT, "resident")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pointTransferEnabled").value(true))
                .andExpect(jsonPath("$.data.pointTransferEligible").value(false))
                .andExpect(jsonPath("$.data.pointTransferBlockedReason",
                        containsString("入住未满2年")));
    }

    // ==================== 2.5 会话楼长标识 ====================

    @Test
    @DisplayName("2.5 GET /messages/conversations：下发 isUnitLeader / leaderRole")
    void conversation_shouldExposeUnitLeaderFields() throws Exception {
        mockMvc.perform(get("/messages/conversations")
                        .header("Authorization", token(VIEWER, "resident")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].peerId").value(LEADER))
                .andExpect(jsonPath("$.data.list[0].isBuildingLeader").value(true))
                .andExpect(jsonPath("$.data.list[0].isUnitLeader").value(true))
                .andExpect(jsonPath("$.data.list[0].leaderRole").value("building_leader"));
    }

    // ==================== 2.6 商品店铺字段 ====================

    @Test
    @DisplayName("2.6 GET /products：shopId=商家ID，shopType 区分业主小店，shopStatus 全量枚举")
    void productList_shouldExposeShopRoutingFields() throws Exception {
        mockMvc.perform(get("/products")
                        .param("merchantId", RESIDENT_SHOP_ID)
                        .param("propertyCompanyId", PC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].shopId").value(RESIDENT_SHOP_ID))
                .andExpect(jsonPath("$.data.list[0].shopName").value("张阿姨的小店"))
                .andExpect(jsonPath("$.data.list[0].shopType").value("resident"))
                .andExpect(jsonPath("$.data.list[0].shopStatus").value("active"))
                .andExpect(jsonPath("$.data.list[0].shopOpen").value(true))
                .andExpect(jsonPath("$.data.list[0].shopOwnerResidentId").value(BUYER));

        mockMvc.perform(get("/products")
                        .param("merchantId", SHOP_ID)
                        .param("propertyCompanyId", PC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].shopType").value("merchant"))
                .andExpect(jsonPath("$.data.list[0].shopOwnerResidentId").doesNotExist());

        mockMvc.perform(get("/products/prd_v823_resident"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shopType").value("resident"))
                .andExpect(jsonPath("$.data.shopOwnerResidentId").value(BUYER));
    }

    @Test
    @DisplayName("推荐商家列表：业主小店返回 shopType=resident，前端可分流")
    void recommendedMerchants_shouldExposeShopType() throws Exception {
        mockMvc.perform(get("/merchants/official-recommended")
                        .param("propertyCompanyId", PC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id == '" + RESIDENT_SHOP_ID + "')].shopType",
                        hasItem("resident")))
                .andExpect(jsonPath("$.data.list[?(@.id == '" + RESIDENT_SHOP_ID + "')].residentId",
                        hasItem(BUYER)));
    }

    // ==================== 2.7 货源供给范围 ====================

    @Test
    @DisplayName("2.7 供给范围：company 代理可自设，platform 仍需管理员授予（条目34）")
    void supplyScope_shouldBeAdminControlled() throws Exception {
        mockMvc.perform(get("/distributors/my/products")
                        .header("Authorization", token(DIST_USER, "merchant")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].supplyScope").value("company"));

        // 条目34：工作台创建货源时会带上 company，此前非空即 403，导致「创建分销商品」必失败
        mockMvc.perform(post("/distributors/my/products")
                        .header("Authorization", token(DIST_USER, "merchant"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"测试货源company范围\",\"wholesalePrice\":10.00,"
                                + "\"suggestedRetailPrice\":15.00,\"supplyScope\":\"company\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supplyScope").value("company"));

        mockMvc.perform(post("/distributors/my/products")
                        .header("Authorization", token(DIST_USER, "merchant"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"测试货源\",\"wholesalePrice\":10.00,"
                                + "\"suggestedRetailPrice\":15.00,\"supplyScope\":\"platform\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("管理员")));

        mockMvc.perform(put("/admin/distributor-products/dpr_v823_1")
                        .header("Authorization", token("res_v823_admin", "property_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supplyScope\":\"platform\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supplyScope").value("platform"));

        mockMvc.perform(get("/distributors/my/products")
                        .header("Authorization", token(DIST_USER, "merchant")))
                .andExpect(status().isOk())
                // 按 id 定位，不用 list[0]：上面新建的 company 货源会排进首位
                .andExpect(jsonPath("$.data.list[?(@.id == 'dpr_v823_1')].supplyScope",
                        org.hamcrest.Matchers.hasItem("platform")));
    }

    // ==================== 2.8 代理购买记录 ====================

    @Test
    @DisplayName("2.8 GET /distributors/my/orders：分页结构、脱敏、关键词与时间筛选、状态枚举")
    void distributorOrders_shouldSupportPagingSearchAndMasking() throws Exception {
        mockMvc.perform(get("/distributors/my/orders")
                        .header("Authorization", token(DIST_USER, "merchant")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.total").value(2))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20))
                .andExpect(jsonPath("$.data.list[0].buyerName").value("张*"))
                .andExpect(jsonPath("$.data.list[0].buyerPhone").value("138****8000"))
                .andExpect(jsonPath("$.data.list[0].shopName").value("张阿姨的小店"))
                .andExpect(jsonPath("$.data.list[0].settlementOwner").value("distributor"));

        mockMvc.perform(get("/distributors/my/orders")
                        .param("keyword", "大米")
                        .header("Authorization", token(DIST_USER, "merchant")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.total").value(1))
                .andExpect(jsonPath("$.data.list[0].productName").value("五常大米 5kg"));

        mockMvc.perform(get("/distributors/my/orders")
                        .param("startDate", LocalDate.now().minusDays(2).toString())
                        .header("Authorization", token(DIST_USER, "merchant")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.total").value(1));

        mockMvc.perform(get("/distributors/my/orders")
                        .param("orderStatus", "shipped")
                        .header("Authorization", token(DIST_USER, "merchant")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/distributors/my/orders")
                        .param("startDate", "2026/10/01")
                        .header("Authorization", token(DIST_USER, "merchant")))
                .andExpect(status().isBadRequest());
    }

    // ==================== 2.9 历史转账流水 ====================

    @Test
    @DisplayName("2.9 GET /points/records：transfer 同时命中历史 transfer / family_transfer")
    void pointRecords_shouldMatchLegacyTransferSource() throws Exception {
        mockMvc.perform(get("/points/records")
                        .param("source", "transfer")
                        .header("Authorization", token(VIEWER, "resident")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.total").value(2));

        mockMvc.perform(get("/points/records")
                        .param("source", "family_transfer")
                        .header("Authorization", token(VIEWER, "resident")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.total").value(2));
    }

    // ==================== helpers ====================

    private Resident saveResident(String id, String role, String phone, String name, int yearsAgo) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setName(name);
        resident.setPhone(phone);
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId("com_v823");
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPointBalance(5000);
        resident.setCoinBalance(new BigDecimal("50.00"));
        resident.setCoinFrozen(false);
        Resident saved = residentRepository.saveAndFlush(resident);
        // @CreatedDate 审计会在 persist 时覆盖手工赋值，测试里用 JDBC 固定入住时间
        LocalDateTime createdAt = yearsAgo > 0
                ? LocalDateTime.now().minusYears(yearsAgo)
                : LocalDateTime.now().minusDays(10);
        jdbcTemplate.update("update residents set created_at = ? where id = ?",
                Timestamp.valueOf(createdAt), id);
        return saved;
    }

    private Merchant saveMerchant(String id, String name, String source, String phone, String residentId) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setPlatformMerchantId("pmm_" + id);
        merchant.setPropertyCompanyId(PC);
        merchant.setName(name);
        merchant.setCategory("外卖小吃");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setMerchantSource(source);
        merchant.setContactPhone(phone);
        merchant.setResidentId(residentId);
        merchant.setDeliveryFee(new BigDecimal("3.00"));
        merchant.setFreeDeliverySponsor("merchant");
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        return merchantRepository.save(merchant);
    }

    private void saveProduct(String id, String merchantId, String name, BigDecimal price, int daysAgo) {
        Product product = new Product();
        product.setId(id);
        product.setMerchantId(merchantId);
        product.setName(name);
        product.setPrice(price);
        product.setStock(100);
        product.setCategory("食品");
        product.setStatus("active");
        product.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        productRepository.save(product);
    }

    private void saveOrder(String id, String orderNo, String productName, BigDecimal amount,
                           LocalDateTime createdAt, String address) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setResidentId(BUYER);
        order.setMerchantId(DIST_ID);
        order.setDistributorId(DIST_ID);
        order.setResidentMerchantId(RESIDENT_SHOP_ID);
        order.setSourceType("distributor");
        order.setSettlementOwner("distributor");
        order.setPropertyCompanyId(PC);
        order.setOrderStatus("completed");
        order.setTotalAmount(amount);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setPaymentMethod("wechat");
        order.setAddress(address);
        order.setContactPhone(BUYER_PHONE);
        order.setCreatedAt(createdAt);
        order.setUpdatedAt(createdAt);
        orderRepository.saveAndFlush(order);
        // 同理：审计会覆盖 createdAt，用 JDBC 固定下单时间，才能验证日期筛选
        jdbcTemplate.update("update orders set created_at = ? where id = ?",
                Timestamp.valueOf(createdAt), id);

        OrderItem item = new OrderItem();
        item.setId("oi_" + id);
        item.setOrderId(id);
        item.setProductId("prd_v823_order");
        item.setProductName(productName);
        item.setQuantity(2);
        item.setUnitPrice(amount.divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP));
        item.setTotalPrice(amount);
        item.setCreatedAt(createdAt);
        orderItemRepository.save(item);
    }

    private String token(String userId, String role) {
        return bearer(userId, role, PC, null);
    }
}
