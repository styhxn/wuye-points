package com.wuye.points.controller;

import com.wuye.points.entity.Announcement;
import com.wuye.points.entity.Product;
import com.wuye.points.repository.AnnouncementRepository;
import com.wuye.points.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 公告接口集成测试：验证“令牌 → AuthInterceptor → UserContext → RoleGuard → Service → 200”完整链路。
 *
 * 说明：公告列表接口在 Service 内部要求登录，因此使用 platform_admin 令牌走通
 * （platform_admin 分支不依赖居民行，空库下直接返回空分页 200），无需任何种子数据。
 *
 * 全程本地 H2 + 容器内 JwtUtil，不连接任何外部/生产服务。
 */
@DisplayName("公告接口集成测试")
class AnnouncementControllerTest extends ControllerTestBase {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AnnouncementRepository announcementRepository;

    @AfterEach
    void cleanUp() {
        announcementRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /announcements 携带 platform_admin 令牌返回 200（空库空分页）")
    void listAnnouncements_authenticated_ok() throws Exception {
        mockMvc.perform(get("/announcements")
                        .header("Authorization", bearer("u_admin", "platform_admin", "pc1", null)))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    @DisplayName("POST /announcements 社区管理员未授权 → 403（非 /admin 路径同样受权限拦截）")
    void createAnnouncement_communityAdminWithoutPermission_forbidden() throws Exception {
        mockMvc.perform(post("/announcements")
                        .header("Authorization", bearer("res_ca", "community_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("POST /announcements/product 路由存在：商品不存在返回 404 而非 405 请求方法不支持")
    void createProductAnnouncement_unknownProduct_returnsNotFoundNotMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/announcements/product")
                        .header("Authorization", bearer("u_admin", "platform_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody("mch_001", "prd_missing")))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.code").value(60004));
    }

    @Test
    @DisplayName("POST /announcements/product 发布成功：落库类型 merchant、content_type product、带商品ID")
    void createProductAnnouncement_ok_persistsMerchantTypeAndProductContentType() throws Exception {
        saveProduct("prd_001", "mch_001", "农夫山泉 550ml");

        mockMvc.perform(post("/announcements/product")
                        .header("Authorization", bearer("u_admin", "platform_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody("mch_001", "prd_001")))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.announcementType").value("merchant"))
                .andExpect(jsonPath("$.data.contentType").value("product"))
                .andExpect(jsonPath("$.data.productId").value("prd_001"));

        Announcement saved = announcementRepository.findAll().get(0);
        assertEquals("merchant", saved.getAnnouncementType());
        assertEquals("product", saved.getContentType());
        assertEquals("prd_001", saved.getProductId());
        assertEquals("mch_001", saved.getMerchantId());
        assertEquals("published", saved.getStatus());
    }

    @Test
    @DisplayName("POST /announcements/product 商家ID与商品归属不一致：400 且不落库，避免公告挂错商家")
    void createProductAnnouncement_merchantMismatch_badRequest() throws Exception {
        saveProduct("prd_003", "mch_003", "洗洁精");

        mockMvc.perform(post("/announcements/product")
                        .header("Authorization", bearer("u_admin", "platform_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody("mch_other", "prd_003")))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        assertEquals(0L, announcementRepository.count());
    }

    @Test
    @DisplayName("POST /announcements/product 缺 productId：400 参数缺失，不落库")
    void createProductAnnouncement_missingProductId_badRequest() throws Exception {
        mockMvc.perform(post("/announcements/product")
                        .header("Authorization", bearer("u_admin", "platform_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"标题\",\"content\":\"内容\",\"productId\":\"\"}"))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        assertEquals(0L, announcementRepository.count());
    }

    @Test
    @DisplayName("POST /announcements/product 管理端身份需 announcement:publish 权限，缺权限 403")
    void createProductAnnouncement_adminWorkspaceWithoutPermission_forbidden() throws Exception {
        saveProduct("prd_002", "mch_002", "抽纸");

        mockMvc.perform(post("/announcements/product")
                        .header("Authorization", bearer("res_ca", "community_admin", "pc1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody("mch_002", "prd_002")))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    private String productBody(String merchantId, String productId) {
        return "{\"title\":\"农夫山泉特惠\",\"content\":\"整箱购买优惠\","
                + "\"communityId\":\"com_001\",\"merchantId\":\"" + merchantId + "\","
                + "\"productId\":\"" + productId + "\",\"coverUrl\":\"/static/cover.png\"}";
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
