package com.wuye.points.repository;

import com.wuye.points.entity.ResidentMerchantApplication;
import com.wuye.points.repository.spec.ResidentMerchantApplicationSpecifications;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 业主小店申请列表 keyword 检索：必须同时覆盖住户姓名/手机号与店铺名称。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("业主小店申请 keyword 检索")
class ResidentMerchantApplicationSearchTest {

    @Autowired
    private ResidentMerchantApplicationRepository applicationRepository;

    private ResidentMerchantApplication application(String id, String residentId, String shopName) {
        ResidentMerchantApplication app = new ResidentMerchantApplication();
        app.setId(id);
        app.setResidentId(residentId);
        app.setPropertyCompanyId("pc_001");
        app.setDepositAmount(new BigDecimal("100.00"));
        app.setStatus("pending_audit");
        app.setVisibility("private");
        app.setShopName(shopName);
        return app;
    }

    @Test
    @DisplayName("只按店名命中时也能搜到")
    void keyword_matchesShopNameOnly() {
        applicationRepository.save(application("app_1", "res_1", "水果小铺"));
        applicationRepository.save(application("app_2", "res_2", "维修铺"));

        List<ResidentMerchantApplication> result = applicationRepository.findAll(
                ResidentMerchantApplicationSpecifications.withFilters(
                        "pc_001", "pending_audit", List.of(), "水果"));

        assertEquals(1, result.size());
        assertEquals("app_1", result.get(0).getId());
    }

    @Test
    @DisplayName("住户姓名/手机号与店名是 OR 关系")
    void keyword_matchesResidentOrShopName() {
        applicationRepository.save(application("app_1", "res_1", "水果小铺"));
        applicationRepository.save(application("app_2", "res_2", "维修铺"));

        List<ResidentMerchantApplication> result = applicationRepository.findAll(
                ResidentMerchantApplicationSpecifications.withFilters(
                        "pc_001", "pending_audit", List.of("res_2"), "水果"));

        assertEquals(2, result.size());
    }
}
