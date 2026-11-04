package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.interceptor.PermissionInterceptor;
import com.wuye.points.util.AdminAuthorizationContext;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理端社区论坛路径到权限码的映射回归。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PermissionInterceptor 社区论坛映射测试")
class PermissionInterceptorTest {

    @Mock
    private PermissionGuard permissionGuard;
    @Mock
    private RoleGuard roleGuard;

    private PermissionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new PermissionInterceptor(permissionGuard, roleGuard);
        UserContext.set(UserContext.builder()
                .userId("res_leader")
                .role("resident")
                .propertyCompanyId("pc_001")
                .build());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        AdminAuthorizationContext.clear();
    }

    @Test
    @DisplayName("GET 社区帖子列表 → community:read")
    void communityPostsGet_mapsToRead() {
        assertTrue(interceptor.preHandle(request("GET", "/api/v1/admin/community/posts"), null, null));
        verify(permissionGuard).requirePermission("community:read");
    }

    @Test
    @DisplayName("GET 帖子评论列表 → community:read")
    void communityCommentsGet_mapsToRead() {
        assertTrue(interceptor.preHandle(
                request("GET", "/api/v1/admin/community/posts/cp_001/comments"), null, null));
        verify(permissionGuard).requirePermission("community:read");
    }

    @Test
    @DisplayName("DELETE 删帖 → community:manage")
    void communityPostDelete_mapsToManage() {
        assertTrue(interceptor.preHandle(
                request("DELETE", "/api/v1/admin/community/posts/cp_001"), null, null));
        verify(permissionGuard).requirePermission("community:manage");
    }

    @Test
    @DisplayName("POST 禁言 → community:manage")
    void forumBanPost_mapsToManage() {
        assertTrue(interceptor.preHandle(
                request("POST", "/api/v1/admin/community/users/res_001/forum-ban"), null, null));
        verify(permissionGuard).requirePermission("community:manage");
    }

    @Test
    @DisplayName("GET 举报列表 → community:read")
    void reportsGet_mapsToRead() {
        assertTrue(interceptor.preHandle(request("GET", "/api/v1/admin/community/reports"), null, null));
        verify(permissionGuard).requirePermission("community:read");
    }

    @Test
    @DisplayName("模块前缀规则：优惠券读/写都走 promotion:push")
    void couponsModule_mapsReadAndWrite() {
        interceptor.preHandle(request("GET", "/api/v1/admin/coupons"), null, null);
        verify(permissionGuard).requirePermission("promotion:push");
        interceptor.preHandle(request("POST", "/api/v1/admin/coupons"), null, null);
        verify(permissionGuard, org.mockito.Mockito.times(2)).requirePermission("promotion:push");
    }

    @Test
    @DisplayName("积分池：GET 走 pool:read，POST 调整走 pool:adjust")
    void pointPools_mapsReadAndAdjust() {
        interceptor.preHandle(request("GET", "/api/v1/admin/point-pools/records"), null, null);
        verify(permissionGuard).requirePermission("pool:read");
        interceptor.preHandle(request("POST", "/api/v1/admin/point-pools/adjust"), null, null);
        verify(permissionGuard).requirePermission("pool:adjust");
    }

    @Test
    @DisplayName("统筹管理：GET 走 coordinator:read，写走 coordinator:manage")
    void coordinators_mapsReadAndManage() {
        interceptor.preHandle(request("GET", "/api/v1/admin/coordinators/coo_001"), null, null);
        verify(permissionGuard).requirePermission("coordinator:read");
        interceptor.preHandle(request("PUT", "/api/v1/admin/coordinators/coo_001"), null, null);
        verify(permissionGuard).requirePermission("coordinator:manage");
    }

    @Test
    @DisplayName("活动组审核走 activity_group:manage")
    void activityGroups_mapsManage() {
        interceptor.preHandle(request("GET", "/api/v1/admin/activity-groups/reviews"), null, null);
        verify(permissionGuard).requirePermission("activity_group:manage");
    }

    @Test
    @DisplayName("最长前缀优先：community-entities 不被 community 规则覆盖")
    void communityEntities_longestPrefixWins() {
        interceptor.preHandle(request("GET", "/api/v1/admin/community-entities"), null, null);
        verify(permissionGuard).requirePermission("community_entity:manage");
    }

    @Test
    @DisplayName("商家子路径：广告额度走 promotion:push，踢出走 merchant:kickout")
    void merchantSubPaths_useSpecificCodes() {
        interceptor.preHandle(request("GET", "/api/v1/admin/merchants/m_001/ad-quota"), null, null);
        verify(permissionGuard).requirePermission("promotion:push");
        interceptor.preHandle(request("POST", "/api/v1/admin/merchants/m_001/kick"), null, null);
        verify(permissionGuard).requirePermission("merchant:kickout");
    }

    @Test
    @DisplayName("平台商家推荐排序走 rank:drag")
    void platformMerchantRecommend_mapsRankDrag() {
        interceptor.preHandle(request("POST", "/api/v1/admin/platform-merchants/m_001/recommend"), null, null);
        verify(permissionGuard).requirePermission("rank:drag");
    }

    @Test
    @DisplayName("物业币显示开关走 coin:display_control")
    void coinDisplay_mapsDisplayControl() {
        interceptor.preHandle(request("PUT", "/api/v1/admin/property-companies/pc_001/coin-display"), null, null);
        verify(permissionGuard).requirePermission("coin:display_control");
    }

    @Test
    @DisplayName("物业公司参数保存走 config:set")
    void propertyCompanyConfig_mapsConfigSet() {
        interceptor.preHandle(request("PATCH", "/api/v1/admin/property-companies/pc_001/config"), null, null);
        verify(permissionGuard).requirePermission("config:set");
    }

    @Test
    @DisplayName("物业币使用条件走 config:set")
    void coinUseCondition_mapsConfigSet() {
        interceptor.preHandle(request("PUT", "/api/v1/admin/property-companies/pc_001/coin-use-condition"), null, null);
        verify(permissionGuard).requirePermission("config:set");
    }

    @Test
    @DisplayName("定向消息统计走 directed_message:stats，不被 {id} 详情规则覆盖")
    void directedMessageStats_mapsStats() {
        interceptor.preHandle(request("GET", "/api/v1/admin/directed-messages/stats"), null, null);
        verify(permissionGuard).requirePermission("directed_message:stats");
    }

    @Test
    @DisplayName("提现设置走 config:set，提现审核走 coin:audit")
    void coinWithdrawals_settingsAndAudit() {
        interceptor.preHandle(request("PUT", "/api/v1/admin/coin-withdrawals/settings"), null, null);
        verify(permissionGuard).requirePermission("config:set");
        interceptor.preHandle(request("GET", "/api/v1/admin/coin-withdrawals"), null, null);
        verify(permissionGuard).requirePermission("coin:audit");
    }

    @Test
    @DisplayName("OPTIONS 预检不校验权限")
    void optionsRequest_skipsPermissionCheck() {
        interceptor.preHandle(request("OPTIONS", "/api/v1/admin/coupons"), null, null);
        verify(permissionGuard, never()).requirePermission(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("非 /admin 公告写操作：管理端身份按 announcement:publish 校验")
    void announcementWrite_byAdminWorkspace_mapsPublish() {
        when(roleGuard.isAdminWorkspaceUser()).thenReturn(true);

        interceptor.preHandle(request("POST", "/api/v1/announcements"), null, null);
        verify(permissionGuard).requirePermission("announcement:publish");
    }

    @Test
    @DisplayName("非 /admin 公告写操作：住户身份不受管理端权限拦截影响")
    void announcementWrite_byResident_skipsAdminPermission() {
        when(roleGuard.isAdminWorkspaceUser()).thenReturn(false);

        interceptor.preHandle(request("POST", "/api/v1/announcements"), null, null);
        verify(permissionGuard, never()).requirePermission(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("报表接口：管理端身份按 reports:view 校验")
    void reports_byAdminWorkspace_mapsReportsView() {
        when(roleGuard.isAdminWorkspaceUser()).thenReturn(true);

        interceptor.preHandle(request("GET", "/api/v1/reports/overview"), null, null);
        verify(permissionGuard).requirePermission("reports:view");
    }

    @Test
    @DisplayName("授权成功后写入请求级标记，供业务层角色门放行标签身份")
    void successfulAuthorization_marksContext() {
        interceptor.preHandle(request("GET", "/api/v1/admin/coupons"), null, null);
        org.junit.jupiter.api.Assertions.assertTrue(AdminAuthorizationContext.isGranted());

        interceptor.afterCompletion(request("GET", "/api/v1/admin/coupons"), null, null, null);
        org.junit.jupiter.api.Assertions.assertFalse(AdminAuthorizationContext.isGranted());
    }

    @Test
    @DisplayName("权限配置中的 44 个权限码都有管理端路由映射")
    @SuppressWarnings("unchecked")
    void allPermissionCodes_haveAdminRouteMapping() throws Exception {
        java.util.Set<String> mapped = new java.util.HashSet<>();

        java.lang.reflect.Field exactField =
                PermissionInterceptor.class.getDeclaredField("ADMIN_PATH_PERMISSIONS");
        exactField.setAccessible(true);
        ((java.util.Map<String, String>) exactField.get(null)).values()
                .forEach(code -> mapped.add(code.replace(':', '.')));

        java.lang.reflect.Field operationField =
                PermissionInterceptor.class.getDeclaredField("ADMIN_OPERATION_PERMISSIONS");
        operationField.setAccessible(true);
        ((java.util.Map<String, String>) operationField.get(null)).values()
                .forEach(code -> mapped.add(code.replace(':', '.')));

        java.lang.reflect.Field rulesField =
                PermissionInterceptor.class.getDeclaredField("MODULE_RULES");
        rulesField.setAccessible(true);
        for (Object rule : (java.util.List<?>) rulesField.get(null)) {
            java.lang.reflect.Method read = rule.getClass().getDeclaredMethod("readCode");
            java.lang.reflect.Method manage = rule.getClass().getDeclaredMethod("manageCode");
            read.setAccessible(true);
            manage.setAccessible(true);
            mapped.add(((String) read.invoke(rule)).replace(':', '.'));
            mapped.add(((String) manage.invoke(rule)).replace(':', '.'));
        }

        java.util.List<String> permissionCodes = java.util.List.of(
                "announcement.publish", "merchant.audit", "merchant.kickout", "rank.drag",
                "promotion.push", "activity_group.manage", "sector.manage", "individual.manage",
                "delivery.grab", "data.view", "notice.collect", "refund.approve",
                "points.input", "coin.freeze", "coin.display_control", "config.set",
                "property_fee.manage", "reports.view", "user.manage", "permission.manage",
                "permission.read", "pool.read", "pool.adjust", "coordinator.read",
                "coordinator.manage", "sector.read", "distribution.read", "coin.audit",
                "point.clear_run", "log.read", "merchant.create", "merchant.read",
                "merchant.update", "property_company.manage", "directed_message.send",
                "directed_message.read", "directed_message.stats", "community_entity.manage",
                "property_operator.manage", "consultation.manage", "resident_merchant.audit",
                "distributor.manage", "community.read", "community.manage");

        java.util.List<String> missing = permissionCodes.stream()
                .filter(code -> !mapped.contains(code))
                .toList();
        org.junit.jupiter.api.Assertions.assertTrue(missing.isEmpty(),
                "以下权限码没有管理端路由映射: " + missing);
    }

    private MockHttpServletRequest request(String method, String uri) {
        return new MockHttpServletRequest(method, uri);
    }
}
