package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 平台交易明细集成测试：真实 Spring 上下文 + H2，验证 UNION 账本 SQL、权限与 CSV。
 */
@DisplayName("平台交易明细集成测试")
class AdminTransactionIntegrationTest extends ControllerTestBase {

    private static final String PC_ID = "pc_tx_test";
    private static final String RESIDENT_ID = "res_tx_test";
    private static final String PAYMENT_ID = "pr_tx_test";
    private static final String REFUND_ID = "pr_tx_refund";
    private static final String WALLET_ID = "wf_tx_test";
    private static final String POINT_ID = "pt_tx_test";
    private static final String START = "2026-10-01";
    private static final String END = "2026-10-31";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void insertData() {
        cleanData();
        jdbcTemplate.update("INSERT INTO property_companies (id, name) VALUES (?, ?)",
                PC_ID, "测试物业");
        jdbcTemplate.update("INSERT INTO residents (id, name, property_company_id, role) VALUES (?, ?, ?, ?)",
                RESIDENT_ID, "测试住户", PC_ID, "resident");

        LocalDateTime paymentTime = LocalDateTime.of(2026, 10, 20, 10, 0);
        jdbcTemplate.update("""
                        INSERT INTO payment_records
                        (id, biz_type, biz_id, resident_id, property_company_id, amount, cash_amount,
                         point_used, coin_used, directed_balance_used, status, out_trade_no,
                         channel_trade_no, paid_at, created_at, updated_at)
                        VALUES (?, 'order', 'ord_tx_test', ?, ?, 100.00, 90.00, 500, 5.00, 0.00,
                                'paid', 'otn_tx_test', 'wx_tx_test', ?, ?, ?)
                        """,
                PAYMENT_ID, RESIDENT_ID, PC_ID, paymentTime, paymentTime, paymentTime);

        LocalDateTime refundTime = LocalDateTime.of(2026, 10, 22, 11, 0);
        jdbcTemplate.update("""
                        INSERT INTO payment_records
                        (id, biz_type, biz_id, resident_id, property_company_id, amount, cash_amount,
                         status, out_trade_no, out_refund_no, paid_at, created_at, updated_at)
                        VALUES (?, 'order', 'ord_tx_refund', ?, ?, 50.00, 50.00,
                                'refunded', 'otn_tx_refund', 'rf_tx_test', ?, ?, ?)
                        """,
                REFUND_ID, RESIDENT_ID, PC_ID, paymentTime, paymentTime, refundTime);

        jdbcTemplate.update("""
                        INSERT INTO wallet_flows
                        (id, owner_type, owner_id, entry_type, amount, balance_type, idempotency_key,
                         created_at, updated_at)
                        VALUES (?, 'property', ?, 'property_product_share', 20.00, 'withdrawable',
                                'tx:test:wallet', ?, ?)
                        """,
                WALLET_ID, PC_ID, LocalDateTime.of(2026, 10, 21, 9, 0), LocalDateTime.of(2026, 10, 21, 9, 0));

        jdbcTemplate.update("""
                        INSERT INTO point_records
                        (id, resident_id, property_company_id, point_type, amount, balance, source,
                         source_id, description, created_at, updated_at)
                        VALUES (?, ?, ?, 'spend', -30, 470, 'order', 'ord_tx_test',
                                '=cmd|calc', ?, ?)
                        """,
                POINT_ID, RESIDENT_ID, PC_ID, LocalDateTime.of(2026, 10, 23, 12, 0),
                LocalDateTime.of(2026, 10, 23, 12, 0));
    }

    @AfterEach
    void cleanData() {
        jdbcTemplate.update("DELETE FROM point_records WHERE id = ?", POINT_ID);
        jdbcTemplate.update("DELETE FROM wallet_flows WHERE id = ?", WALLET_ID);
        jdbcTemplate.update("DELETE FROM payment_records WHERE id IN (?, ?)", PAYMENT_ID, REFUND_ID);
        jdbcTemplate.update("DELETE FROM residents WHERE id = ?", RESIDENT_ID);
        jdbcTemplate.update("DELETE FROM property_companies WHERE id = ?", PC_ID);
    }

    @Test
    @DisplayName("platform_admin 可分页查看四类账本，payment/refund 分别成行")
    void list_platformAdmin_returnsMergedLedger() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/transactions")
                        .header("Authorization", bearer("u_tx_admin", "platform_admin", PC_ID, null))
                        .param("propertyCompanyId", PC_ID)
                        .param("startDate", START)
                        .param("endDate", END)
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = readData(result);
        // 5 行：原支付(paid)、原支付(refunded 仍计收入)、退款支出、钱包收入、积分支出
        assertEquals(5, data.path("pagination").path("total").asInt());

        Map<String, JsonNode> byKey = new HashMap<>();
        data.path("list").forEach(node ->
                byKey.put(node.path("category").asText() + ":" + node.path("id").asText(), node));

        JsonNode payment = byKey.get("payment:" + PAYMENT_ID);
        assertEquals("income", payment.path("direction").asText());
        assertEquals("测试住户", payment.path("partyName").asText());
        assertEquals("测试物业", payment.path("propertyCompanyName").asText());
        assertEquals(0, new BigDecimal("90").compareTo(payment.path("netAmount").decimalValue()));

        JsonNode refund = byKey.get("refund:" + REFUND_ID);
        assertEquals("expense", refund.path("direction").asText());
        assertEquals("退款", refund.path("categoryLabel").asText());

        JsonNode wallet = byKey.get("wallet:" + WALLET_ID);
        assertEquals("income", wallet.path("direction").asText());
        assertEquals("测试物业", wallet.path("partyName").asText());

        JsonNode point = byKey.get("point:" + POINT_ID);
        assertEquals("expense", point.path("direction").asText());
        assertEquals("积分", point.path("unitLabel").asText());
        assertEquals(0, new BigDecimal("30").compareTo(point.path("amount").decimalValue()));
    }

    @Test
    @DisplayName("分类/分组/关键词筛选与分页生效")
    void list_filtersAndPagination() throws Exception {
        JsonNode payment = readData(mockMvc.perform(get("/admin/transactions")
                        .header("Authorization", bearer("u_tx_admin", "platform_admin", PC_ID, null))
                        .param("propertyCompanyId", PC_ID)
                        .param("startDate", START)
                        .param("endDate", END)
                        .param("category", "payment"))
                .andExpect(status().isOk())
                .andReturn());
        // payment 分类含 paid/refunded 两条原支付流水；退款单独在 refund 分类
        assertEquals(2, payment.path("pagination").path("total").asInt());

        JsonNode valueGroup = readData(mockMvc.perform(get("/admin/transactions")
                        .header("Authorization", bearer("u_tx_admin", "platform_admin", PC_ID, null))
                        .param("propertyCompanyId", PC_ID)
                        .param("startDate", START)
                        .param("endDate", END)
                        .param("group", "VALUE"))
                .andExpect(status().isOk())
                .andReturn());
        assertEquals(1, valueGroup.path("pagination").path("total").asInt());

        JsonNode keyword = readData(mockMvc.perform(get("/admin/transactions")
                        .header("Authorization", bearer("u_tx_admin", "platform_admin", PC_ID, null))
                        .param("propertyCompanyId", PC_ID)
                        .param("startDate", START)
                        .param("endDate", END)
                        .param("keyword", WALLET_ID))
                .andExpect(status().isOk())
                .andReturn());
        assertEquals(1, keyword.path("pagination").path("total").asInt());

        JsonNode page2 = readData(mockMvc.perform(get("/admin/transactions")
                        .header("Authorization", bearer("u_tx_admin", "platform_admin", PC_ID, null))
                        .param("propertyCompanyId", PC_ID)
                        .param("startDate", START)
                        .param("endDate", END)
                        .param("page", "2")
                        .param("pageSize", "3"))
                .andExpect(status().isOk())
                .andReturn());
        assertEquals(2, page2.path("list").size());
    }

    @Test
    @DisplayName("总览按分类/单位汇总，积分支出计为 expense")
    void overview_aggregatesByCategoryAndUnit() throws Exception {
        JsonNode data = readData(mockMvc.perform(get("/admin/transactions/overview")
                        .header("Authorization", bearer("u_tx_admin", "platform_admin", PC_ID, null))
                        .param("propertyCompanyId", PC_ID)
                        .param("startDate", START)
                        .param("endDate", END))
                .andExpect(status().isOk())
                .andReturn());

        Map<String, JsonNode> categories = new HashMap<>();
        data.path("categories").forEach(node -> categories.put(node.path("category").asText(), node));

        // 原支付 100 + 已退款订单的原支付 50，退款 50 单列支出
        assertEquals(0, new BigDecimal("150").compareTo(
                categories.get("payment").path("incomeAmount").decimalValue()));
        assertEquals(0, new BigDecimal("50").compareTo(
                categories.get("refund").path("expenseAmount").decimalValue()));
        assertEquals(0, new BigDecimal("20").compareTo(
                categories.get("wallet").path("incomeAmount").decimalValue()));
        assertEquals(0, new BigDecimal("30").compareTo(
                categories.get("point").path("expenseAmount").decimalValue()));
        assertEquals("POINT", categories.get("point").path("unit").asText());

        assertTrue(data.path("trend").size() >= 1);
    }

    @Test
    @DisplayName("CSV 导出带 BOM、表头，且公式注入被转义")
    void export_csvContainsBomAndEscapesFormula() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/transactions/export")
                        .header("Authorization", bearer("u_tx_admin", "platform_admin", PC_ID, null))
                        .param("propertyCompanyId", PC_ID)
                        .param("startDate", START)
                        .param("endDate", END))
                .andExpect(status().isOk())
                .andReturn();

        String csv = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFF交易时间"));
        assertTrue(csv.contains("支付"));
        assertTrue(csv.contains("'=cmd|calc"));
        assertEquals("false", result.getResponse().getHeader("X-Export-Truncated"));
        assertEquals("5", result.getResponse().getHeader("X-Export-Rows"));
    }

    @Test
    @DisplayName("物业管理员访问交易明细返回 403")
    void list_propertyAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/admin/transactions")
                        .header("Authorization", bearer("u_tx_property", "property_admin", PC_ID, null))
                        .param("propertyCompanyId", PC_ID))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("非法分类与超大分页返回 400")
    void list_invalidParams_badRequest() throws Exception {
        mockMvc.perform(get("/admin/transactions")
                        .header("Authorization", bearer("u_tx_admin", "platform_admin", PC_ID, null))
                        .param("category", "unknown"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/admin/transactions")
                        .header("Authorization", bearer("u_tx_admin", "platform_admin", PC_ID, null))
                        .param("pageSize", "101"))
                .andExpect(status().isBadRequest());
    }

    private JsonNode readData(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(body);
        assertNotNull(root.path("data"));
        return root.path("data");
    }
}
