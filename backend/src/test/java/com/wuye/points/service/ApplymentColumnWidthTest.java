package com.wuye.points.service;

import com.wuye.points.dto.merchant.ApplymentSubmitRequest;
import com.wuye.points.entity.MerchantApplyment;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 进件表列宽回归（2026-11-05 线上 409）。
 *
 * <p>故障：merchant_applyments.bank_account_type 是 varchar(20)，而官方枚举
 * {@code BANK_ACCOUNT_TYPE_CORPORATE} 有 27 个字符，保存草稿时 MySQL 抛
 * Data truncation，被全局处理器兜底成 409「数据冲突，请刷新后重试」。</p>
 *
 * <p>本测试锁死三件事：枚举值放得进列、请求体每个字符串都有长度上限、
 * 请求体上限不超过实体列宽。生产表列宽由 sql/manual_upgrade_20261105_applyment_column_width.sql 保证。</p>
 */
class ApplymentColumnWidthTest {

    @Test
    @DisplayName("所有会落库的枚举值都必须放得进对应 varchar 列")
    void enumValuesFitEntityColumns() throws Exception {
        assertFits("bankAccountType", "BANK_ACCOUNT_TYPE_CORPORATE");
        assertFits("bankAccountType", "BANK_ACCOUNT_TYPE_PERSONAL");
        assertFits("organizationType", "ENTERPRISE");
        assertFits("organizationType", "INDIVIDUAL");
        assertFits("salesScenesType", "SALES_SCENES_MINI_PROGRAM");
        assertFits("salesScenesType", "SALES_SCENES_STORE");
        assertFits("salesScenesType", "SALES_SCENES_MP");
        assertFits("contactType", "LEGAL");
        assertFits("applymentState", "legal_validating");
        assertFits("submitMode", "manual");
    }

    @Test
    @DisplayName("请求体每个 String 字段都有 @Size：超长在入库前返回 400 而不是 409")
    void dtoStringFieldsAllHaveSizeLimit() {
        for (Field f : ApplymentSubmitRequest.class.getDeclaredFields()) {
            if (f.getType() != String.class) {
                continue;
            }
            Size size = f.getAnnotation(Size.class);
            assertNotNull(size, () -> "字段 " + f.getName() + " 缺少 @Size(max)，超长内容会落到数据库变 409");
            assertTrue(size.max() > 0, () -> "字段 " + f.getName() + " 的 @Size(max) 必须为正数");
        }
    }

    @Test
    @DisplayName("请求体长度上限不得超过实体同名 varchar 列宽")
    void dtoSizeLimitsWithinEntityColumns() {
        Map<String, Field> entityFields = new HashMap<>();
        for (Field f : MerchantApplyment.class.getDeclaredFields()) {
            entityFields.put(f.getName(), f);
        }
        for (Field f : ApplymentSubmitRequest.class.getDeclaredFields()) {
            if (f.getType() != String.class) {
                continue;
            }
            Field entityField = entityFields.get(f.getName());
            if (entityField == null) {
                // 敏感字段在实体里是 *_enc（密文列），明文长度上限不受该列约束
                continue;
            }
            Column column = entityField.getAnnotation(Column.class);
            if (column == null || column.columnDefinition().contains("text")) {
                continue;
            }
            Size size = f.getAnnotation(Size.class);
            assertNotNull(size, () -> "字段 " + f.getName() + " 缺少 @Size(max)");
            assertTrue(size.max() <= column.length(),
                    () -> "字段 " + f.getName() + " 允许 " + size.max() + " 字符，但实体列只有 "
                            + column.length() + " 字符，入库会 Data truncation");
        }
    }

    private static void assertFits(String entityFieldName, String value) throws Exception {
        Column column = MerchantApplyment.class.getDeclaredField(entityFieldName).getAnnotation(Column.class);
        assertNotNull(column, () -> "实体字段 " + entityFieldName + " 缺少 @Column");
        assertTrue(value.length() <= column.length(),
                () -> entityFieldName + " 列宽 " + column.length() + " 存不下枚举值 "
                        + value + "（" + value.length() + " 字符）");
    }
}
