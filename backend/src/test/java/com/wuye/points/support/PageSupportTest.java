package com.wuye.points.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分页统一构造的回归测试。
 *
 * <p>这些断言同时锁定了历史缺陷：17 个 Service 各自实现 buildPageable 时，
 * 有的 page&lt;1 抛异常、有的静默修正，有的校验排序白名单、有的静默吞掉非法字段。
 */
class PageSupportTest {

    private static final Set<String> SORTABLE = Set.of("createdAt", "totalAmount");

    @Test
    @DisplayName("page 小于 1 时修正为第一页，而不是抛异常")
    void pageLessThanOneIsClamped() {
        Pageable pageable = PageSupport.build(0, 20, null);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    @DisplayName("pageSize 被限制在上限内，防止超大分页打爆内存")
    void pageSizeIsCapped() {
        assertEquals(PageSupport.DEFAULT_MAX_PAGE_SIZE,
                PageSupport.build(1, Integer.MAX_VALUE, null).getPageSize());
        assertEquals(50, PageSupport.build(1, 500, null, null, 50).getPageSize());
    }

    @Test
    @DisplayName("pageSize 小于 1 时修正为 1")
    void pageSizeLessThanOneIsClamped() {
        assertEquals(1, PageSupport.build(1, 0, null).getPageSize());
    }

    @Test
    @DisplayName("无排序表达式时使用默认排序（创建时间倒序）")
    void blankSortFallsBackToDefault() {
        Sort sort = PageSupport.build(1, 10, "  ").getSort();
        assertEquals("createdAt", sort.getOrderFor("createdAt").getProperty());
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("createdAt").getDirection());
    }

    @Test
    @DisplayName("支持 -field 降序与 +field 升序两种前缀")
    void supportsPrefixSyntax() {
        assertEquals(Sort.Direction.DESC,
                PageSupport.parseSort("-createdAt", SORTABLE).getOrderFor("createdAt").getDirection());
        assertEquals(Sort.Direction.ASC,
                PageSupport.parseSort("+createdAt", SORTABLE).getOrderFor("createdAt").getDirection());
    }

    @Test
    @DisplayName("支持 field,asc|desc 形式")
    void supportsCommaSyntax() {
        assertEquals(Sort.Direction.ASC,
                PageSupport.parseSort("totalAmount,asc", SORTABLE).getOrderFor("totalAmount").getDirection());
        assertEquals(Sort.Direction.DESC,
                PageSupport.parseSort("totalAmount,desc", SORTABLE).getOrderFor("totalAmount").getDirection());
    }

    @Test
    @DisplayName("白名单外的排序字段一律回退默认排序，杜绝客户端注入任意属性名")
    void rejectsFieldOutsideWhitelist() {
        Sort sort = PageSupport.parseSort("-idCard", SORTABLE);
        assertEquals("createdAt", sort.getOrderFor("createdAt").getProperty());
        assertTrue(sort.getOrderFor("totalAmount") == null);
    }

    @Test
    @DisplayName("可指定自定义默认排序（如公告默认按发布时间）")
    void supportsCustomDefaultSort() {
        Sort custom = Sort.by(Sort.Direction.DESC, "publishedAt");
        Sort sort = PageSupport.parseSort("badField", Set.of("publishedAt"), custom);
        assertEquals("publishedAt", sort.getOrderFor("publishedAt").getProperty());
    }
}
