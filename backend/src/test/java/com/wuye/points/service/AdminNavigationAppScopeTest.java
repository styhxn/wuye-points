package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.NavigationItem;
import com.wuye.points.repository.NavigationItemRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * 手机端导航下发范围（整改清单条目31）：小程序首页只传 module、不传 position，
 * 并把返回当成整组导航直接渲染，因此管理端配好并启用的导航项必须能被查到。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("手机端导航下发范围")
class AdminNavigationAppScopeTest {

    @Mock
    private NavigationItemRepository navigationItemRepository;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private AdminNavigationService service;

    private NavigationItem item(String id, String position, String module, String title) {
        NavigationItem item = new NavigationItem();
        item.setId(id);
        item.setPosition(position);
        item.setModule(module);
        item.setTitle(title);
        item.setLinkType("page");
        item.setLinkUrl("/pages/announcement/list");
        item.setSortOrder(1);
        item.setEnabled(true);
        item.setPropertyCompanyId("pc_demo001");
        return item;
    }

    @BeforeEach
    void setUp() {
        UserContext.set(UserContext.builder().userId("res_1").role("resident")
                .propertyCompanyId("pc_demo001").build());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("租户只配了 top 位：首页按 module 查询仍要拿到该条（条目31 现场故障）")
    void topOnly_stillReturnedForModuleQuery() {
        when(navigationItemRepository.findByPropertyCompanyIdAndEnabledTrueOrderBySortOrderAsc("pc_demo001"))
                .thenReturn(List.of(item("nav_1", "top", "home", "公告")));
        when(navigationItemRepository.findByPropertyCompanyIdIsNullAndEnabledTrueOrderBySortOrderAsc())
                .thenReturn(List.of());

        List<NavigationItem> result = service.listForApp("home", null);

        assertEquals(1, result.size());
        assertEquals("公告", result.get(0).getTitle());
    }

    @Test
    @DisplayName("bottom 与 top 同时配置：不传 position 全下发，按 sortOrder 已在仓储层排好")
    void bothPositions_returnedWhenPositionOmitted() {
        when(navigationItemRepository.findByPropertyCompanyIdAndEnabledTrueOrderBySortOrderAsc("pc_demo001"))
                .thenReturn(List.of(item("nav_1", "bottom", "home", "周围商家"),
                        item("nav_2", "top", "home", "公告")));

        assertEquals(2, service.listForApp("home", null).size());
    }

    @Test
    @DisplayName("显式要 top 时才返回追加位")
    void explicitTop_returnsAppendSlots() {
        when(navigationItemRepository.findByPropertyCompanyIdAndEnabledTrueOrderBySortOrderAsc("pc_demo001"))
                .thenReturn(List.of(item("nav_1", "top", "home", "公告")));

        assertEquals(1, service.listForApp("home", "top").size());
    }

    @Test
    @DisplayName("租户配了 bottom：整组主导航按配置下发")
    void tenantBottom_takeover() {
        when(navigationItemRepository.findByPropertyCompanyIdAndEnabledTrueOrderBySortOrderAsc("pc_demo001"))
                .thenReturn(List.of(item("nav_1", "bottom", "home", "周围商家"),
                        item("nav_2", "bottom", "home", "外卖")));

        assertEquals(2, service.listForApp("home", null).size());
    }

    @Test
    @DisplayName("租户未配 bottom 时回落平台默认 bottom")
    void fallbackToPlatformDefaults() {
        when(navigationItemRepository.findByPropertyCompanyIdAndEnabledTrueOrderBySortOrderAsc("pc_demo001"))
                .thenReturn(List.of());
        NavigationItem platformItem = item("nav_9", "bottom", "home", "周围商家");
        platformItem.setPropertyCompanyId(null);
        when(navigationItemRepository.findByPropertyCompanyIdIsNullAndEnabledTrueOrderBySortOrderAsc())
                .thenReturn(List.of(platformItem, item("nav_8", "top", "home", "公告")));

        List<NavigationItem> result = service.listForApp("home", null);

        // 本物业 home 模块一条都没配 → 回落平台默认；不传 position 时 top/bottom 都下发
        assertEquals(2, result.size());
        assertEquals("周围商家", result.get(0).getTitle());
    }

    @Test
    @DisplayName("本物业只配了别的模块：home 仍回落平台默认，不被自己别的模块挡掉")
    void otherModuleOnly_stillFallsBackForHome() {
        when(navigationItemRepository.findByPropertyCompanyIdAndEnabledTrueOrderBySortOrderAsc("pc_demo001"))
                .thenReturn(List.of(item("nav_1", "top", "service", "服务")));
        NavigationItem platformItem = item("nav_9", "bottom", "home", "周围商家");
        platformItem.setPropertyCompanyId(null);
        when(navigationItemRepository.findByPropertyCompanyIdIsNullAndEnabledTrueOrderBySortOrderAsc())
                .thenReturn(List.of(platformItem));

        List<NavigationItem> result = service.listForApp("home", null);

        assertEquals(1, result.size());
        assertEquals("周围商家", result.get(0).getTitle());
    }

    @Test
    @DisplayName("管理端列表按模块过滤（页面下拉框传 module）")
    void adminList_filtersByModule() {
        when(navigationItemRepository.findByPropertyCompanyIdOrderBySortOrderAsc("pc_demo001"))
                .thenReturn(List.of(item("nav_1", "top", "home", "公告"),
                        item("nav_2", "top", "service", "服务")));

        assertEquals(1, service.list("pc_demo001", "home").size());
        assertEquals(2, service.list("pc_demo001", null).size());
    }

    @Test
    @DisplayName("序列化必须带 linkTarget：App 侧按 linkTarget/target/url/path 取跳转目标")
    void linkTargetAlias_exposed() {
        assertEquals("/pages/announcement/list", item("nav_1", "top", "home", "公告").getLinkTarget());
    }
}
