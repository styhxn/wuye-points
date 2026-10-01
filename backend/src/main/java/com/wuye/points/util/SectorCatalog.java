package com.wuye.points.util;

import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * 板块（sector）枚举字典 —— 全局唯一数据源。
 *
 * <p>2026-09-08 前端问题「板块负责人改板块 400」根因：管理端两处 Service 各自定义了一份
 * 与文档不一致的旧枚举（elderly/medical/education/cleaning/canteen/pet/life_service），
 * 与文档 §39 / 前端五枚举（cleaning/repair/security/greening/other）只有 cleaning 重合，
 * 因此除保洁外全部被判为「sector不合法」。
 *
 * <p>现统一收敛到本类：所有板块读写入口（管理端板块负责人、个体负责人、统筹工作台、
 * 住户个体负责人申请、分账统计标签）一律以 {@link #SECTORS} 为准，并支持中文入参归一化。
 */
public final class SectorCatalog {

    /** 文档 §39 / §37 约定的五个板块编码 */
    public static final Set<String> SECTORS = Set.of(
            "cleaning", "repair", "security", "greening", "other");

    /** 板块编码 -> 中文名 */
    private static final Map<String, String> LABELS = Map.of(
            "cleaning", "保洁",
            "repair", "维修",
            "security", "安保",
            "greening", "绿化",
            "other", "其它");

    /**
     * 历史/中文别名 -> 标准编码。
     * 仅用于入参兼容，不会写库；历史库中若存在旧编码，{@link #label} 走兼容展示。
     */
    private static final Map<String, String> ALIASES = Map.of(
            "保洁", "cleaning",
            "维修", "repair",
            "维修板块", "repair",
            "安保", "security",
            "保安", "security",
            "绿化", "greening",
            "其它", "other",
            "其他", "other");

    /** 兼容展示：历史数据中的旧板块编码 */
    private static final Map<String, String> LEGACY_LABELS = Map.of(
            "elderly", "养老板块",
            "medical", "医疗板块",
            "education", "教育板块",
            "canteen", "食堂板块",
            "pet", "宠物板块",
            "life_service", "生活服务");

    private SectorCatalog() {
    }

    public static boolean isValid(String sector) {
        return sector != null && SECTORS.contains(sector);
    }

    /**
     * 归一化：中文名/大小写/前后空格 -> 标准英文编码。
     * 已是标准编码时原样返回；无法识别时返回 trim 后的原始值（由 validate 报错）。
     */
    public static String normalize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String trimmed = raw.trim();
        String aliased = ALIASES.get(trimmed);
        if (aliased != null) {
            return aliased;
        }
        return trimmed.toLowerCase();
    }

    /**
     * 校验并归一化板块：空值原样返回（表示"不修改"），非法值抛 400。
     */
    public static String normalizeOrThrow(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String normalized = normalize(raw);
        if (!isValid(normalized)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "sector不合法：" + raw + "；可选值 cleaning/repair/security/greening/other（保洁/维修/安保/绿化/其它）");
        }
        return normalized;
    }

    /** 校验（允许为空，空表示不修改/不过滤） */
    public static void validate(String sector) {
        if (StringUtils.hasText(sector)) {
            normalizeOrThrow(sector);
        }
    }

    public static String label(String sector) {
        if (sector == null) {
            return null;
        }
        String label = LABELS.get(sector);
        if (label != null) {
            return label;
        }
        return LEGACY_LABELS.getOrDefault(sector, sector);
    }
}
