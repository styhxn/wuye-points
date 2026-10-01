package com.wuye.points.controller;

import com.wuye.points.entity.ActivityGroup;
import com.wuye.points.entity.ActivityGroupMember;
import com.wuye.points.entity.DistributorProduct;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Message;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantListing;
import com.wuye.points.repository.ActivityGroupMemberRepository;
import com.wuye.points.repository.ActivityGroupRepository;
import com.wuye.points.repository.DistributorProductRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentMerchantListingRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;

/**
 * 前端问题汇总（7 项）修复验证：
 *  - POST /orders/preview（§8.0）
 *  - GET /property-coins/use-condition（§79.1）
 *  - GET /messages 对 merchant/activity_leader 放行（§12.5）
 *  - GET /messages/conversations 与 {peerId}（§30.2/30.3）
 *  - POST/PUT /products 对 activity_leader 放行（§6.3/6.4）
 *  - PUT /merchants/my/delivery-fees（§7.2.1）与 PUT /merchants/{id}（§7.5）
 *  - PUT /activity-groups/{id}/members/{memberId}/profile（§68.1）
 *  - GET /distributors/my/pickups 返回 residentName（§94.4）
 */
@DisplayName("前端问题汇总修复验证")
class FrontendIssueFixesTest extends ControllerTestBase {

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
    private ActivityGroupRepository activityGroupRepository;
    @Autowired
    private ActivityGroupMemberRepository activityGroupMemberRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private DistributorProductRepository distributorProductRepository;
    @Autowired
    private ResidentMerchantListingRepository listingRepository;

    @BeforeEach
    void seed() {
        propertyCompanyRepository.deleteAll();
        merchantPropertyConfigRepository.deleteAll();
        productRepository.deleteAll();
        listingRepository.deleteAll();
        distributorProductRepository.deleteAll();
        merchantRepository.deleteAll();
        activityGroupMemberRepository.deleteAll();
        activityGroupRepository.deleteAll();
        messageRepository.deleteAll();
        residentRepository.deleteAll();

        PropertyCompany company = new PropertyCompany();
        company.setId("pc_demo001");
        company.setName("演示物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        saveResident("res_dev001", "resident", "13900000001", "张阿姨");
        saveResident("res_act01", "activity_leader", "13900000011", "郑美丽");
        saveResident("res_mch03", "merchant", "13900000009", "超市老板");
        saveResident("res_dist01", "merchant", "13900000007", "一级经销商");

        // 组长店（§6.3/6.4/7.5 场景）
        Merchant groupMerchant = saveMerchant("mch_act01", "郑美丽的小店", "group_leader", "13900000011", "res_act01");
        // 商家店（§7.2.1 场景）
        Merchant normalMerchant = saveMerchant("mch_demo001", "便民超市", "platform", "13900000009", "res_mch03");
        // 一级经销商店（§94.4 场景）
        Merchant distributor = saveMerchant("mch_dist01", "精选货源", "platform", "13900000007", "res_dist01");
        distributor.setMerchantLevel("official_certified");
        merchantRepository.save(distributor);

        saveConfig("mpc_test001", normalMerchant.getId());
        saveConfig("mpc_test002", groupMerchant.getId());

        Product product = new Product();
        product.setId("prd_issue001");
        product.setMerchantId(groupMerchant.getId());
        product.setName("可口可乐330ml");
        product.setPrice(new BigDecimal("3.50"));
        product.setMemberPrice(new BigDecimal("3.00"));
        product.setStock(100);
        product.setCategory("饮料");
        product.setStatus("active");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        // 便民超市商家名下商品（§6.1 关键词按商家名命中的验证）
        Product fmProduct = new Product();
        fmProduct.setId("prd_issue002");
        fmProduct.setMerchantId(normalMerchant.getId());
        fmProduct.setName("土豆1斤");
        fmProduct.setPrice(new BigDecimal("2.00"));
        fmProduct.setStock(200);
        fmProduct.setCategory("生鲜");
        fmProduct.setStatus("active");
        fmProduct.setCreatedAt(LocalDateTime.now());
        fmProduct.setUpdatedAt(LocalDateTime.now());
        productRepository.save(fmProduct);

        ActivityGroup group = new ActivityGroup();
        group.setId("act_issue001");
        group.setName("广场舞团");
        group.setLeaderId("res_act01");
        group.setPropertyCompanyId("pc_demo001");
        group.setMemberCount(1);
        group.setStatus("active");
        group.setReviewStatus("approved");
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        activityGroupRepository.save(group);

        ActivityGroupMember member = new ActivityGroupMember();
        member.setId("agm_issue001");
        member.setGroupId(group.getId());
        member.setResidentId("res_dev001");
        member.setRole("member");
        member.setStatus("active");
        member.setJoinedAt(LocalDateTime.now());
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        activityGroupMemberRepository.save(member);

        // 商家与业主的私聊（§12.5 / §30.x 场景）
        Message msg = new Message();
        msg.setId("msg_issue001");
        msg.setFromId("res_mch03");
        msg.setToId("res_dev001");
        msg.setMessageType("community_message");
        msg.setContent("你好，请问需要什么？");
        msg.setChatType("private");
        msg.setReadStatus("unread");
        msg.setPropertyCompanyId("pc_demo001");
        msg.setCreatedAt(LocalDateTime.now());
        msg.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(msg);

        // 组长与组员的私聊
        Message msg2 = new Message();
        msg2.setId("msg_issue002");
        msg2.setFromId("res_act01");
        msg2.setToId("res_dev001");
        msg2.setMessageType("community_message");
        msg2.setContent("明天活动记得来");
        msg2.setChatType("private");
        msg2.setReadStatus("unread");
        msg2.setPropertyCompanyId("pc_demo001");
        msg2.setCreatedAt(LocalDateTime.now());
        msg2.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(msg2);

        // 一级货源与拿货记录（§94.4 场景）
        DistributorProduct dp = new DistributorProduct();
        dp.setId("dp_issue001");
        dp.setDistributorMerchantId(distributor.getId());
        dp.setName("东北大米10kg");
        dp.setWholesalePrice(new BigDecimal("50.00"));
        dp.setSuggestedRetailPrice(new BigDecimal("60.00"));
        dp.setStock(100);
        dp.setStatus("active");
        dp.setCreatedAt(LocalDateTime.now());
        dp.setUpdatedAt(LocalDateTime.now());
        distributorProductRepository.save(dp);

        ResidentMerchantListing listing = new ResidentMerchantListing();
        listing.setId("rml_issue001");
        listing.setResidentMerchantId("rm_issue001");
        listing.setResidentId("res_dev001");
        listing.setDistributorProductId(dp.getId());
        listing.setRetailPrice(new BigDecimal("65.00"));
        listing.setEnabled(true);
        listing.setShareCount(0);
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    private void saveResident(String id, String role, String phone, String name) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setName(name);
        resident.setPhone(phone);
        resident.setPropertyCompanyId("pc_demo001");
        resident.setRole(role);
        resident.setStatus("active");
        resident.setPointBalance(5000);
        resident.setCoinBalance(new BigDecimal("50.00"));
        resident.setCoinFrozen(false);
        residentRepository.save(resident);
    }

    private void saveConfig(String id, String merchantId) {
        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(id);
        config.setMerchantId(merchantId);
        config.setPropertyCompanyId("pc_demo001");
        config.setCommissionRate(new BigDecimal("0.1500"));
        config.setPointExchangeRate(new BigDecimal("100.0000"));
        config.setCoinRebateRate(BigDecimal.ZERO);
        config.setCoinRebateEnabled(false);
        config.setStatus("active");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        merchantPropertyConfigRepository.save(config);
    }

    private Merchant saveMerchant(String id, String name, String source, String phone, String residentId) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setPlatformMerchantId("pm_" + id);
        merchant.setPropertyCompanyId("pc_demo001");
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

    private String token(String userId, String role) {
        return "Bearer " + jwtUtil.generateAccessToken(userId, role, "pc_demo001", null);
    }

    // ==================== 业主支付：preview + use-condition ====================

    @Test
    @DisplayName("POST /orders/preview: resident 返回 code=0 且含 cashAmount/directedBalances")
    void previewOrder_ok() throws Exception {
        mockMvc.perform(post("/orders/preview")
                        .header("Authorization", token("res_dev001", "resident"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantId\":\"mch_act01\",\"items\":[{\"productId\":\"prd_issue001\",\"quantity\":2}],\"coinUsed\":2.00,\"pointUsed\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalAmount").isNumber())
                .andExpect(jsonPath("$.data.cashAmount").isNumber())
                .andExpect(jsonPath("$.data.directedBalances").isArray());
    }

    @Test
    @DisplayName("GET /property-coins/use-condition: resident 返回 code=0，无配置时 condition=none")
    void useCondition_ok() throws Exception {
        mockMvc.perform(get("/property-coins/use-condition")
                        .header("Authorization", token("res_dev001", "resident")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.condition").value("none"))
                .andExpect(jsonPath("$.data.satisfied").value(true))
                .andExpect(jsonPath("$.data.eligible").value(true));
    }

    // ==================== 商家消息：GET /messages 放行 ====================

    @Test
    @DisplayName("GET /messages: merchant 返回 200 且 list 含本人相关消息（不再 403）")
    void merchantListMessages_ok() throws Exception {
        mockMvc.perform(get("/messages")
                        .param("messageType", "community_message")
                        .param("chatType", "private")
                        .header("Authorization", token("res_mch03", "merchant")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list[0].content").value("你好，请问需要什么？"));
    }

    @Test
    @DisplayName("GET /messages: activity_leader 返回 200（不再 403）")
    void leaderListMessages_ok() throws Exception {
        mockMvc.perform(get("/messages")
                        .param("messageType", "community_message")
                        .param("chatType", "private")
                        .header("Authorization", token("res_act01", "activity_leader")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list[0].content").value("明天活动记得来"));
    }

    // ==================== 会话列表/历史 ====================

    @Test
    @DisplayName("GET /messages/conversations: activity_leader 返回会话列表")
    void leaderConversations_ok() throws Exception {
        mockMvc.perform(get("/messages/conversations")
                        .header("Authorization", token("res_act01", "activity_leader")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list[0].peerId").value("res_dev001"));
    }

    @Test
    @DisplayName("GET /messages/conversations/{peerId}: 返回历史并将未读标记已读")
    void conversationHistory_ok() throws Exception {
        mockMvc.perform(get("/messages/conversations/res_act01")
                        .header("Authorization", token("res_dev001", "resident")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list[0].content").value("明天活动记得来"));
    }

    // ==================== 组长商品创建/编辑 ====================

    @Test
    @DisplayName("POST /products: activity_leader 返回 code=0（不再 403）")
    void leaderCreateProduct_ok() throws Exception {
        mockMvc.perform(post("/products")
                        .header("Authorization", token("res_act01", "activity_leader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"矿泉水\",\"price\":2.00,\"stock\":50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isString());
    }

    @Test
    @DisplayName("PUT /products/{id}: activity_leader 改价返回 code=0（不再 403）")
    void leaderUpdateProduct_ok() throws Exception {
        mockMvc.perform(put("/products/prd_issue001")
                        .header("Authorization", token("res_act01", "activity_leader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":4.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 组长配送费 ====================

    @Test
    @DisplayName("PUT /merchants/my/delivery-fees: activity_leader 返回 code=0（不再 404/403）")
    void leaderUpdateDeliveryFees_ok() throws Exception {
        mockMvc.perform(put("/merchants/my/delivery-fees")
                        .header("Authorization", token("res_act01", "activity_leader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryFeeTiers\":[{\"minKm\":0,\"maxKm\":1,\"fee\":3,\"enabled\":true}],\"freeDeliveryEnabled\":true,\"freeDeliveryThreshold\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.deliveryFeeTiers[0].fee").value(3));
    }

    @Test
    @DisplayName("PUT /merchants/{id}: activity_leader 改组长店返回 code=0（不再 403）")
    void leaderUpdateMerchant_ok() throws Exception {
        mockMvc.perform(put("/merchants/mch_act01")
                        .header("Authorization", token("res_act01", "activity_leader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryFee\":5.00,\"freeDeliveryEnabled\":true,\"freeDeliveryThreshold\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.freeDeliveryEnabled").value(true));
    }

    // ==================== 组长编辑组员简介 ====================

    @Test
    @DisplayName("PUT /activity-groups/{id}/members/{memberId}/profile: 组长返回 code=0（不再 404）")
    void leaderUpdateMemberProfile_ok() throws Exception {
        mockMvc.perform(put("/activity-groups/act_issue001/members/res_dev001/profile")
                        .header("Authorization", token("res_act01", "activity_leader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"introduction\":\"{\\\"name\\\":\\\"张阿姨\\\",\\\"age\\\":40}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.introduction").value("{\"name\":\"张阿姨\",\"age\":40}"));
    }

    // ==================== 拿货记录拿货人 ====================

    @Test
    @DisplayName("GET /distributors/my/pickups: 返回 residentName/residentPhone（不再缺拿货人）")
    void pickups_includeResidentName() throws Exception {
        mockMvc.perform(get("/distributors/my/pickups")
                        .header("Authorization", token("res_dist01", "merchant")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].residentId").value("res_dev001"))
                .andExpect(jsonPath("$.data[0].residentName").value("张*姨"));
    }

    // ==================== 商城商家关键词搜索（§6.1 2026-09 客户需求）====================

    @Test
    @DisplayName("GET /products?keyword=便民: 命中商家名命中的商品（便民超市名下商品）")
    void products_keywordMatchesMerchantName() throws Exception {
        mockMvc.perform(get("/products").param("keyword", "便民")
                        .param("propertyCompanyId", "pc_demo001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[*].id", hasItem("prd_issue002")));
    }

    // ==================== 官方推荐商家（§admin merchants recommend 2026-09）====================

    @Test
    @DisplayName("POST /admin/merchants/{id}/recommend: 设为官方推荐返回 code=0")
    void adminMerchant_setRecommendation_ok() throws Exception {
        mockMvc.perform(post("/admin/merchants/mch_demo001/recommend")
                        .header("Authorization", token("res_act01", "platform_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isRecommended\":true,\"recommendedSort\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 物业推荐首页（§75.1/75.2：避免 pageSize=200 触发 400）====================

    @Test
    @DisplayName("GET /merchants/official-recommended: 一次全量返回推荐商家（绕过 pageSize≤100 限制）")
    void officialRecommended_listRecommendedMerchants() throws Exception {
        // 先 mch_demo001 设为官方推荐
        mockMvc.perform(post("/admin/merchants/mch_demo001/recommend")
                        .header("Authorization", token("res_act01", "platform_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isRecommended\":true,\"recommendedSort\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 物业推荐首页（无需分页参数）
        mockMvc.perform(get("/merchants/official-recommended")
                        .param("propertyCompanyId", "pc_demo001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[*].id", hasItem("mch_demo001")));
    }
}