package com.wuye.points.support;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * 分页与排序的统一构造。
 *
 * <p>背景：{@code buildPageable} 与 {@code parseSort} 在 17 / 16 个 Service 中被各写一份，
 * 且已经发生"克隆漂移"——同一套 REST 分页协议在不同接口上语义不同：
 * 有的接口 page&lt;1 时抛 400，有的静默修正为第 1 页；有的支持多字段排序与白名单校验，
 * 有的只认 createdAt 并静默吞掉非法字段。前端无法预期，改协议要改 17 处。
 *
 * <p>统一语义（宽容优先，保证既有调用方不会因参数边界而失败）：
 * <ul>
 *   <li>{@code page} 小于 1 时修正为第 1 页；</li>
 *   <li>{@code pageSize} 修正到 [1, maxPageSize]；</li>
 *   <li>排序字段不在白名单内时，整体回退为默认排序（默认按创建时间倒序）；</li>
 *   <li>支持 {@code -field} / {@code +field} 前缀表示降序 / 升序，也支持 {@code field,desc} 形式。</li>
 * </ul>
 */
public final class PageSupport {

    /** 单页最大条数上限，防止 pageSize=Integer.MAX_VALUE 打爆内存 */
    public static final int DEFAULT_MAX_PAGE_SIZE = 100;

    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private PageSupport() {
    }

    /**
     * 构造分页请求，排序字段不做白名单限制（调用方已确保字段合法时使用）。
     */
    public static Pageable build(int page, int pageSize, String sort) {
        return build(page, pageSize, sort, null, DEFAULT_MAX_PAGE_SIZE);
    }

    /**
     * 构造分页请求，并校验排序字段是否在白名单内。
     *
     * @param page         1 起始的页码
     * @param pageSize     每页条数
     * @param sort         排序表达式，如 {@code "-createdAt"} 或 {@code "totalAmount,desc"}
     * @param sortableFields 允许的排序字段白名单，为 null 时不做校验
     * @param maxPageSize  每页条数上限
     */
    public static Pageable build(int page, int pageSize, String sort,
                                 Set<String> sortableFields, int maxPageSize) {
        return build(page, pageSize, sort, sortableFields, maxPageSize, defaultSort());
    }

    /**
     * 构造分页请求：可指定每页上限与默认排序（各列表页的默认字段并不相同，
     * 例如公告默认按发布时间、社区服务默认按排序值）。
     *
     * @param page            1 起始的页码
     * @param pageSize        每页条数
     * @param sort            排序表达式
     * @param sortableFields  排序字段白名单，为 null 时不做校验
     * @param maxPageSize     每页条数上限，小于等于 0 时使用 {@link #DEFAULT_MAX_PAGE_SIZE}
     * @param defaultSort     排序表达式为空或非法时使用的默认排序
     */
    public static Pageable build(int page, int pageSize, String sort,
                                 Set<String> sortableFields, int maxPageSize, Sort defaultSort) {
        int safePage = Math.max(page, 1);
        int upper = maxPageSize > 0 ? maxPageSize : DEFAULT_MAX_PAGE_SIZE;
        int safePageSize = Math.min(Math.max(pageSize, 1), upper);
        return PageRequest.of(safePage - 1, safePageSize, parseSort(sort, sortableFields, defaultSort));
    }

    /** 默认排序：按创建时间倒序 */
    public static Sort defaultSort() {
        return Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD);
    }

    /**
     * 解析排序表达式。
     *
     * <p>兼容三种写法：{@code -field}（降序）、{@code +field}（升序）、{@code field,asc|desc}。
     * 字段不在白名单内或表达式非法时，回退为按创建时间倒序。
     */
    public static Sort parseSort(String sort, Set<String> sortableFields) {
        return parseSort(sort, sortableFields, defaultSort());
    }

    /**
     * 解析排序表达式。
     *
     * <p>兼容三种写法：{@code -field}（降序）、{@code +field}（升序）、{@code field,asc|desc}。
     * 字段不在白名单内或表达式非法时，回退为指定的默认排序。
     *
     * <p><b>安全约束</b>：排序字段名会进入 JPQL / SQL，必须始终传入白名单；
     * 不传白名单（null）仅可用于排序字段完全由服务端内部构造的场景。
     */
    public static Sort parseSort(String sort, Set<String> sortableFields, Sort defaultSort) {
        if (sort == null || sort.isBlank()) {
            return defaultSort;
        }
        String expression = sort.trim();
        Sort.Direction direction = Sort.Direction.DESC;
        String field = expression;

        if (expression.startsWith("-")) {
            direction = Sort.Direction.DESC;
            field = expression.substring(1);
        } else if (expression.startsWith("+")) {
            direction = Sort.Direction.ASC;
            field = expression.substring(1);
        } else if (expression.contains(",")) {
            String[] parts = expression.split(",", 2);
            field = parts[0].trim();
            direction = "asc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
        }

        if (field.isEmpty()) {
            return defaultSort;
        }
        if (sortableFields != null && !sortableFields.contains(field)) {
            return defaultSort;
        }
        return Sort.by(direction, field);
    }
}
