package com.wuye.points.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 注册链路公开读回归（清单外新发现，但它卡住条目 38/39/40）。
 *
 * <p>线上现象：小程序注册页「所属物业公司」显示「暂无可选物业公司」。真因不是没有数据，
 * 而是 {@code GET /property-companies} 与 {@code GET /property-companies/{id}/communities}
 * 在 service 层要求管理员角色，注册中的新住户被 403（nginx 日志实证），
 * 前端把权限失败显示成「没有公司可选」。</p>
 *
 * <p>修法按身份分档：管理员看全量；其他身份与未登录只拿选择所需字段且强制 active。
 * 断言按「字段值为 null」写——生产 {@code default-property-inclusion: non_null}
 * 会把 null 键整个去掉，测试库未继承该配置，因此不能断言键不存在。</p>
 */
@DisplayName("注册页物业公司/小区公开读")
class RegisterFlowPublicOptionsTest extends ControllerTestBase {

    private static final String PC_ACTIVE = "pc_regflow1";
    private static final String PC_INACTIVE = "pc_regflow2";
    private static final String COM_ACTIVE = "com_regflow1";
    private static final String COM_INACTIVE = "com_regflow2";
    /** 刚微信登录、还没挂物业公司的新住户（注册页就是这个状态） */
    private static final String NEW_RESIDENT = "res_regflow_new";

    @Autowired
    private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired
    private CommunityRepository communityRepository;
    @Autowired
    private ResidentRepository residentRepository;

    private void seed() {
        propertyCompanyRepository.save(company(PC_ACTIVE, "注册链路测试物业", "active"));
        propertyCompanyRepository.save(company(PC_INACTIVE, "注册链路停用物业", "inactive"));
        communityRepository.save(community(COM_ACTIVE, PC_ACTIVE, "注册链路测试小区", "active"));
        communityRepository.save(community(COM_INACTIVE, PC_ACTIVE, "注册链路停用小区", "inactive"));
    }

    private PropertyCompany company(String id, String name, String status) {
        PropertyCompany company = new PropertyCompany();
        company.setId(id);
        company.setName(name);
        company.setStatus(status);
        company.setContactPhone("13900001111");
        company.setAddress("测试地址");
        return company;
    }

    private Community community(String id, String pcId, String name, String status) {
        Community community = new Community();
        community.setId(id);
        community.setPropertyCompanyId(pcId);
        community.setName(name);
        community.setStatus(status);
        community.setAddress("测试小区地址");
        community.setTotalBuildings(3);
        community.setTotalUnits(9);
        return community;
    }

    private Resident newResidentWithoutProperty() {
        Resident resident = new Resident();
        resident.setId(NEW_RESIDENT);
        resident.setPhone("13970000001");
        resident.setName("注册链路新住户");
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setTokenVersion(0);
        return residentRepository.save(resident);
    }

    @AfterEach
    void cleanUp() {
        communityRepository.deleteAllById(List.of(COM_ACTIVE, COM_INACTIVE));
        propertyCompanyRepository.deleteAllById(List.of(PC_ACTIVE, PC_INACTIVE));
        residentRepository.deleteById(NEW_RESIDENT);
    }

    /** 从响应里按 id 取列表项（必须显式按 UTF-8 读，MockMvc 默认按 ISO-8859-1 解中文会变乱码） */
    private JsonNode findItem(String body, String id) throws Exception {
        JsonNode list = objectMapper.readTree(body).path("data").path("list");
        for (JsonNode node : list) {
            if (id.equals(node.path("id").asText())) {
                return node;
            }
        }
        return null;
    }

    private String contentOf(org.springframework.test.web.servlet.MvcResult result)
            throws java.io.UnsupportedEncodingException {
        return result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    }

    private void assertOmitted(JsonNode item, String field) {
        JsonNode value = item.get(field);
        assertTrue(value == null || value.isNull(), field + " 不应下发，实际=" + value);
    }

    @Test
    @DisplayName("未登录可取物业公司列表，且只回公开字段（无联系电话/地址/统计）")
    void anonymous_canListActiveCompanies_withPublicFieldsOnly() throws Exception {
        seed();

        String body = contentOf(mockMvc.perform(get("/property-companies")
                        .param("page", "1").param("pageSize", "50").param("status", "active"))
                .andExpect(status().isOk())
                .andReturn());

        JsonNode item = findItem(body, PC_ACTIVE);
        assertNotNull(item, "公开档应能取到启用中的物业公司；响应=" + body);
        assertTrue("注册链路测试物业".equals(item.path("name").asText()),
                "name 应下发，实际项=" + item);
        assertOmitted(item, "contactPhone");
        assertOmitted(item, "address");
        assertOmitted(item, "stats");
        assertOmitted(item, "communityCount");
    }

    @Test
    @DisplayName("未登录传 status=inactive 也翻不出停用物业公司（公开档强制 active）")
    void anonymous_cannotSeeInactiveCompanies() throws Exception {
        seed();

        String body = contentOf(mockMvc.perform(get("/property-companies")
                        .param("page", "1").param("pageSize", "50").param("status", "inactive"))
                .andExpect(status().isOk())
                .andReturn());

        assertTrue(findItem(body, PC_INACTIVE) == null, "停用物业公司不应出现在公开档");
        assertNotNull(findItem(body, PC_ACTIVE));
    }

    @Test
    @DisplayName("新住户（已登录、未挂物业）可取物业公司下的小区，且只列启用中的")
    void newResident_canListCommunities() throws Exception {
        seed();
        newResidentWithoutProperty();

        String body = contentOf(mockMvc.perform(get("/property-companies/" + PC_ACTIVE + "/communities")
                        .header("Authorization", bearer(NEW_RESIDENT, "resident", null, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.propertyCompanyId").value(PC_ACTIVE))
                .andReturn());

        JsonNode active = findItem(body, COM_ACTIVE);
        assertNotNull(active, "启用中的小区应可选");
        assertOmitted(active, "totalBuildings");
        assertOmitted(active, "totalUnits");
        assertOmitted(active, "status");
        assertTrue(findItem(body, COM_INACTIVE) == null, "停用小区不应下发给注册页");
    }

    @Test
    @DisplayName("新住户查停用物业公司的小区 → 404，不给翻归属")
    void newResident_inactiveCompanyCommunities_isNotFound() throws Exception {
        seed();
        newResidentWithoutProperty();

        mockMvc.perform(get("/property-companies/" + PC_INACTIVE + "/communities")
                        .header("Authorization", bearer(NEW_RESIDENT, "resident", null, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("物业管理员仍看全量字段（含统计），且看不到别家公司与其小区")
    void propertyAdmin_keepsFullFields_andScope() throws Exception {
        seed();

        String body = contentOf(mockMvc.perform(get("/property-companies")
                        .param("page", "1").param("pageSize", "50")
                        .header("Authorization", bearer("res_regflow_admin", "property_admin", PC_ACTIVE, null)))
                .andExpect(status().isOk())
                .andReturn());

        JsonNode mine = findItem(body, PC_ACTIVE);
        assertNotNull(mine, "物业管理员应仍看到本公司");
        assertTrue(mine.get("stats") != null && !mine.get("stats").isNull(), "管理员档必须保留统计字段");
        assertTrue(findItem(body, PC_INACTIVE) == null, "物业管理员只限本公司");

        mockMvc.perform(get("/property-companies/" + PC_INACTIVE + "/communities")
                        .header("Authorization", bearer("res_regflow_admin", "property_admin", PC_ACTIVE, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("完全未登录也能取小区列表：注册页第一步就是匿名调它（此前被鉴权拦成 401）")
    void anonymous_canListCommunities() throws Exception {
        seed();

        String body = contentOf(mockMvc.perform(get("/property-companies/" + PC_ACTIVE + "/communities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.propertyCompanyId").value(PC_ACTIVE))
                .andReturn());

        JsonNode active = findItem(body, COM_ACTIVE);
        assertNotNull(active, "启用中的小区应可选");
        assertTrue(findItem(body, COM_INACTIVE) == null, "停用小区不应下发给注册页");
        assertOmitted(active, "totalBuildings");
        assertOmitted(active, "status");

        mockMvc.perform(get("/property-companies/" + PC_INACTIVE + "/communities"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("白名单只作用于 GET：匿名写物业公司仍被鉴权挡住")
    void anonymous_cannotWritePropertyCompany() throws Exception {
        seed();

        mockMvc.perform(put("/property-companies/" + PC_ACTIVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"匿名改名\"}"))
                .andExpect(status().isUnauthorized());
    }
}
