package com.wuye.points.controller;

import com.wuye.points.entity.Announcement;
import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantServiceCommunity;
import com.wuye.points.entity.NeighborBlock;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.AnnouncementRepository;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantServiceCommunityRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.NeighborBlockRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 楼长工作台「本单元运营」：发布单元通知 + 分享商品给本单元。
 *
 * <p>重点锁三件事：定向范围只能取自本人楼长记录（前端传不了）、单元可见性按 unit 整段匹配
 * （5单元 不得命中 15单元）、分享范围限本小区且剔除已屏蔽该楼长的住户。</p>
 *
 * <p>全程本地 H2 + 容器内 JwtUtil，不连接任何外部/生产服务。</p>
 */
@DisplayName("楼长单元通知与商品分享测试")
class BuildingLeaderUnitOpsTest extends ControllerTestBase {

    private static final String PC = "pc_blops";
    private static final String COMMUNITY = "com_blops";
    private static final String LEADER = "res_blops_leader";
    private static final String MATE = "res_blops_mate";
    private static final String OTHER_UNIT = "res_blops_other";
    private static final String BLOCKER = "res_blops_blocker";
    private static final String LEADER_PHONE = "13911000001";
    private static final String MERCHANT_ID = "mch_blops_all";
    private static final String BOUND_MERCHANT_ID = "mch_blops_bound";
    private static final String OUT_MERCHANT_ID = "mch_blops_out";
    private static final String PRODUCT_ALL = "prd_blops_all";
    private static final String PRODUCT_BOUND = "prd_blops_bound";
    private static final String PRODUCT_OUT = "prd_blops_out";

    @Autowired
    private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired
    private ResidentRepository residentRepository;
    @Autowired
    private BuildingLeaderRepository buildingLeaderRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantServiceCommunityRepository merchantServiceCommunityRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AnnouncementRepository announcementRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private NeighborBlockRepository neighborBlockRepository;

    @BeforeEach
    void seed() {
        messageRepository.deleteAll();
        neighborBlockRepository.deleteAll();
        announcementRepository.deleteAll();
        productRepository.deleteAll();
        merchantServiceCommunityRepository.deleteAll();
        merchantRepository.deleteAll();
        buildingLeaderRepository.deleteAll();
        residentRepository.deleteAll();
        propertyCompanyRepository.deleteAll();

        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("楼长运营测试物业");
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        // 楼长与两名同单元住户（其中一名已屏蔽楼长），外加一名 15单元住户做越单元对照
        saveResident(LEADER, "3栋", "5单元");
        saveResident(MATE, "3栋", "5单元");
        saveResident(BLOCKER, "3栋", "5单元");
        saveResident(OTHER_UNIT, "3栋", "15单元");

        BuildingLeader leader = new BuildingLeader();
        leader.setId("bl_blops");
        leader.setResidentId(LEADER);
        leader.setPropertyCompanyId(PC);
        leader.setCommunityId(COMMUNITY);
        leader.setUnit("5单元");
        leader.setCommissionRate(new BigDecimal("0.1000"));
        leader.setStatus("active");
        buildingLeaderRepository.save(leader);

        NeighborBlock block = new NeighborBlock();
        block.setId("nb_blops_1");
        block.setBlockerId(BLOCKER);
        block.setBlockedId(LEADER);
        block.setPropertyCompanyId(PC);
        block.setBlockedAt(LocalDateTime.now());
        neighborBlockRepository.save(block);

        saveMerchant(MERCHANT_ID, "全小区通用超市", true);
        saveMerchant(BOUND_MERCHANT_ID, "绑定本小区商家", false);
        saveMerchant(OUT_MERCHANT_ID, "外小区商家", false);
        merchantServiceCommunityRepository.save(bindToCommunity(BOUND_MERCHANT_ID));

        saveProduct(PRODUCT_ALL, MERCHANT_ID, "家政保洁2小时");
        saveProduct(PRODUCT_BOUND, BOUND_MERCHANT_ID, "桶装水配送");
        saveProduct(PRODUCT_OUT, OUT_MERCHANT_ID, "外小区商品");
    }

    // ------------------------------------------------------------------ 发布单元通知

    @Test
    @DisplayName("楼长发布单元通知成功且定向单元取自本人记录")
    void publishNotice_ok() throws Exception {
        mockMvc.perform(post("/building-leaders/my/notices")
                        .header("Authorization", leaderToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"周六楼道清洁\",\"content\":\"请把杂物收进屋内\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contentType").value("notification"))
                .andExpect(jsonPath("$.data.deliveryChannel").value("announcement_board"))
                .andExpect(jsonPath("$.data.status").value("published"))
                .andExpect(jsonPath("$.data.targetUnits", hasSize(1)))
                .andExpect(jsonPath("$.data.targetUnits", hasItem("5单元")));
    }

    @Test
    @DisplayName("非楼长住户发布单元通知 → 404（91004）")
    void publishNotice_notBuildingLeader_notFound() throws Exception {
        mockMvc.perform(post("/building-leaders/my/notices")
                        .header("Authorization", bearer(MATE, "resident", PC, null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"越权\",\"content\":\"不该发出去\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(91004));
    }

    @Test
    @DisplayName("单元通知只对本单元住户可见，15单元看不到")
    void notice_visibilityScopedToUnit() throws Exception {
        String noticeId = publishNotice("本单元停水通知");

        mockMvc.perform(get("/announcements")
                        .header("Authorization", bearer(MATE, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[*].id", hasItem(noticeId)));

        mockMvc.perform(get("/announcements")
                        .header("Authorization", bearer(OTHER_UNIT, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[*].id", not(hasItem(noticeId))));
    }

    @Test
    @DisplayName("跨单元按 id 直取详情 → 404")
    void notice_detailCrossUnitForbidden() throws Exception {
        String noticeId = publishNotice("本单元电梯检修");

        mockMvc.perform(get("/announcements/" + noticeId)
                        .header("Authorization", bearer(OTHER_UNIT, "resident", PC, null)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/announcements/" + noticeId)
                        .header("Authorization", bearer(MATE, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetUnits", hasItem("5单元")));
    }

    @Test
    @DisplayName("未登记单元的住户看不到单元通知（与详情端 404 保持一致）")
    void notice_notVisibleToResidentWithoutUnit() throws Exception {
        String noticeId = publishNotice("单元专属通知");
        saveResident("res_blops_nounit", "3栋", null);

        mockMvc.perform(get("/announcements")
                        .header("Authorization", bearer("res_blops_nounit", "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[*].id", not(hasItem(noticeId))));
    }

    @Test
    @DisplayName("楼长每日通知限3条，第4条 → 400（91009）")
    void publishNotice_dailyLimit() throws Exception {
        for (int i = 1; i <= 3; i++) {
            publishNotice("第" + i + "条通知");
        }
        mockMvc.perform(post("/building-leaders/my/notices")
                        .header("Authorization", leaderToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"第4条\",\"content\":\"超上限\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(91009));
    }

    // ------------------------------------------------------------------ 分享商品给本单元

    @Test
    @DisplayName("可分享商品列表含全小区通用与显式绑定本小区的商家商品，排除外小区")
    void listShareableProducts_scopedToCommunity() throws Exception {
        mockMvc.perform(get("/building-leaders/my/shareable-products")
                        .header("Authorization", leaderToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[*].id", hasItem(PRODUCT_ALL)))
                .andExpect(jsonPath("$.data.list[*].id", hasItem(PRODUCT_BOUND)))
                .andExpect(jsonPath("$.data.list[*].id", not(hasItem(PRODUCT_OUT))));
    }

    @Test
    @DisplayName("分享商品给本单元：落一条产品推荐公告 + 逐户卡片消息，剔除屏蔽者与本人")
    void shareProduct_fansOutCardMessages() throws Exception {
        mockMvc.perform(post("/building-leaders/my/product-shares")
                        .header("Authorization", leaderToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_ALL + "\",\"remark\":\"师傅很靠谱\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recipientCount").value(1))
                .andExpect(jsonPath("$.data.unit").value("5单元"))
                .andExpect(jsonPath("$.data.productName").value("家政保洁2小时"))
                .andExpect(jsonPath("$.data.announcementId").isString());

        // 同单元住户收到卡片消息，且楼长推荐语进到了消息正文
        mockMvc.perform(get("/messages")
                        .header("Authorization", bearer(MATE, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[*].cardType", hasItem("product")))
                .andExpect(jsonPath("$.data.list[*].cardId", hasItem(PRODUCT_ALL)))
                .andExpect(jsonPath("$.data.list[*].content", hasItem(containsString("师傅很靠谱"))));

        // 15单元住户不受影响
        mockMvc.perform(get("/messages")
                        .header("Authorization", bearer(OTHER_UNIT, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(0)));

        // 公告侧同样只在本单元可见
        Announcement announcement = announcementRepository.findAll().stream()
                .filter(item -> "product".equals(item.getContentType()))
                .findFirst()
                .orElseThrow();
        mockMvc.perform(get("/announcements")
                        .header("Authorization", bearer(OTHER_UNIT, "resident", PC, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[*].id", not(hasItem(announcement.getId()))));
    }

    @Test
    @DisplayName("分享非本小区商品 → 400（60051）")
    void shareProduct_outsideCommunity_rejected() throws Exception {
        mockMvc.perform(post("/building-leaders/my/product-shares")
                        .header("Authorization", leaderToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_OUT + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(60051));
    }

    @Test
    @DisplayName("楼长每日分享限3次，第4次 → 400（91010）")
    void shareProduct_dailyLimit() throws Exception {
        for (int i = 1; i <= 3; i++) {
            shareProduct(PRODUCT_BOUND);
        }
        mockMvc.perform(post("/building-leaders/my/product-shares")
                        .header("Authorization", leaderToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_BOUND + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(91010));
    }

    @Test
    @DisplayName("我的运营记录分 contentType 返回，通知与分享互不混入")
    void listMyNotices_separatedByContentType() throws Exception {
        publishNotice("一条通知");
        shareProduct(PRODUCT_ALL);

        mockMvc.perform(get("/building-leaders/my/notices")
                        .header("Authorization", leaderToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].contentType").value("notification"));

        mockMvc.perform(get("/building-leaders/my/product-shares")
                        .header("Authorization", leaderToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].contentType").value("product"))
                .andExpect(jsonPath("$.data.list[0].productId").value(PRODUCT_ALL));
    }

    // ------------------------------------------------------------------ 种子与辅助

    private String leaderToken() {
        return bearer(LEADER, "resident", PC, null);
    }

    private String publishNotice(String title) throws Exception {
        String body = mockMvc.perform(post("/building-leaders/my/notices")
                        .header("Authorization", leaderToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"content\":\"正文\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asText();
    }

    private void shareProduct(String productId) throws Exception {
        mockMvc.perform(post("/building-leaders/my/product-shares")
                        .header("Authorization", leaderToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\"}"))
                .andExpect(status().isOk());
    }

    private void saveResident(String id, String building, String unit) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setName("住户" + id.substring(id.lastIndexOf('_') + 1));
        resident.setPhone("13911" + String.format("%06d", Math.abs(id.hashCode()) % 1000000));
        resident.setPropertyCompanyId(PC);
        resident.setCommunityId(COMMUNITY);
        resident.setBuilding(building);
        resident.setUnit(unit);
        resident.setRoom("01");
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPointBalance(1000);
        resident.setCoinBalance(new BigDecimal("10.00"));
        resident.setCoinFrozen(false);
        residentRepository.saveAndFlush(resident);
    }

    private void saveMerchant(String id, String name, boolean serveAllCommunities) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setPlatformMerchantId("pmm_" + id);
        merchant.setPropertyCompanyId(PC);
        merchant.setName(name);
        merchant.setCategory("家政服务");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setMerchantSource("platform");
        merchant.setContactPhone("13911000999");
        merchant.setServeAllCommunities(serveAllCommunities);
        merchant.setDeliveryFee(new BigDecimal("3.00"));
        merchant.setFreeDeliverySponsor("merchant");
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantRepository.save(merchant);
    }

    private MerchantServiceCommunity bindToCommunity(String merchantId) {
        MerchantServiceCommunity binding = new MerchantServiceCommunity();
        binding.setId("msc_blops_1");
        binding.setMerchantId(merchantId);
        binding.setCommunityId(COMMUNITY);
        return binding;
    }

    private void saveProduct(String id, String merchantId, String name) {
        Product product = new Product();
        product.setId(id);
        product.setMerchantId(merchantId);
        product.setName(name);
        product.setDescription(name + " 描述");
        product.setPrice(new BigDecimal("39.00"));
        product.setStock(100);
        product.setCategory("cleaning");
        product.setStatus("active");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}
