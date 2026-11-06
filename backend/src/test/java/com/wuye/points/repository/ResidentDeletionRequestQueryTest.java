package com.wuye.points.repository;

import com.wuye.points.entity.Resident;
import com.wuye.points.repository.spec.ResidentSpecifications;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 注销终审列表：申请注销时 deletedAt 被用来承载冷静期到期时间（非空），
 * 因此终审查询不能沿用普通列表的 deletedAt is null 条件，否则管理端恒看不到任何申请。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("注销终审列表查询")
class ResidentDeletionRequestQueryTest {

    @Autowired
    private ResidentRepository residentRepository;

    private Resident resident(String id, String status, LocalDateTime deletedAt) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setName("住户" + id);
        resident.setPhone("1390000" + id.substring(id.length() - 4));
        resident.setRole("resident");
        resident.setStatus(status);
        resident.setDeletedAt(deletedAt);
        return resident;
    }

    @Test
    @DisplayName("冷静期账号（deletedAt 非空）能进注销终审列表")
    void coolingPeriodResident_isListed() {
        residentRepository.save(resident("res_del1", "pending_deletion", LocalDateTime.now().plusDays(15)));

        List<Resident> result = residentRepository.findAll(ResidentSpecifications.deletionRequests());

        assertEquals(1, result.size());
        assertEquals("res_del1", result.get(0).getId());
    }

    @Test
    @DisplayName("普通住户列表仍排除冷静期账号")
    void generalList_keepsExcludingCoolingPeriodResident() {
        residentRepository.save(resident("res_del1", "pending_deletion", LocalDateTime.now().plusDays(15)));

        List<Resident> result = residentRepository.findAll(
                ResidentSpecifications.withFilters(null, null, null, null, "pending_deletion", null));

        assertTrue(result.isEmpty(), "withFilters 的 deletedAt is null 条件应排除冷静期账号");
    }

    @Test
    @DisplayName("active 与已注销账号不进终审列表")
    void onlyPendingDeletion_isListed() {
        residentRepository.save(resident("res_act1", "active", null));
        residentRepository.save(resident("res_del1", "deleted", LocalDateTime.now()));
        residentRepository.save(resident("res_pen1", "pending_deletion", LocalDateTime.now().plusDays(15)));

        List<Resident> result = residentRepository.findAll(ResidentSpecifications.deletionRequests());

        assertEquals(1, result.size());
        assertEquals("res_pen1", result.get(0).getId());
    }
}
