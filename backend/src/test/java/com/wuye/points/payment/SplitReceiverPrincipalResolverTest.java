package com.wuye.points.payment;

import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.CourierManager;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.CourierManagerRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 分账个人接收方主体 → 住户账号映射单元测试。
 *
 * <p>这是「按主体自动取 openid」最容易出错的一层：分账记录的 owner_id 存的是各角色表主键，
 * 只有 RESIDENT 与 PROPERTY_MANAGER 直接用 residents.id。映射错一个，钱就会打到别人零钱里。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SplitReceiverPrincipalResolverTest {

    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private CoordinatorRepository coordinatorRepository;
    @Mock
    private SectorLeaderRepository sectorLeaderRepository;
    @Mock
    private IndividualLeaderRepository individualLeaderRepository;
    @Mock
    private BuildingLeaderRepository buildingLeaderRepository;
    @Mock
    private CourierManagerRepository courierManagerRepository;
    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private SplitReceiverPrincipalResolver resolver;

    @Test
    @DisplayName("RESIDENT / PROPERTY_MANAGER 的 ownerId 就是 residents.id")
    void resolveResidentId_directResidentTypes() {
        assertEquals("res_001", resolver.resolveResidentId("RESIDENT", "res_001"));
        assertEquals("res_002", resolver.resolveResidentId("PROPERTY_MANAGER", "res_002"));
    }

    @Test
    @DisplayName("四种负责人与楼长/快递员/商家店主，经各自角色表反查 residentId")
    void resolveResidentId_viaRoleTables() {
        Coordinator coordinator = new Coordinator();
        coordinator.setResidentId("res_coo");
        when(coordinatorRepository.findById("coo_1")).thenReturn(Optional.of(coordinator));
        assertEquals("res_coo", resolver.resolveResidentId("COORDINATOR", "coo_1"));

        SectorLeader sectorLeader = new SectorLeader();
        sectorLeader.setResidentId("res_sl");
        when(sectorLeaderRepository.findById("sl_1")).thenReturn(Optional.of(sectorLeader));
        assertEquals("res_sl", resolver.resolveResidentId("SECTOR_LEADER", "sl_1"));

        IndividualLeader individualLeader = new IndividualLeader();
        individualLeader.setResidentId("res_il");
        when(individualLeaderRepository.findById("il_1")).thenReturn(Optional.of(individualLeader));
        assertEquals("res_il", resolver.resolveResidentId("INDIVIDUAL_LEADER", "il_1"));

        BuildingLeader buildingLeader = new BuildingLeader();
        buildingLeader.setResidentId("res_bl");
        when(buildingLeaderRepository.findById("bl_1")).thenReturn(Optional.of(buildingLeader));
        assertEquals("res_bl", resolver.resolveResidentId("BUILDING_LEADER", "bl_1"));

        CourierManager courier = new CourierManager();
        courier.setResidentId("res_cm");
        when(courierManagerRepository.findById("cm_1")).thenReturn(Optional.of(courier));
        assertEquals("res_cm", resolver.resolveResidentId("COURIER", "cm_1"));

        Merchant merchant = new Merchant();
        merchant.setResidentId("res_mch");
        when(merchantRepository.findById("mch_1")).thenReturn(Optional.of(merchant));
        assertEquals("res_mch", resolver.resolveResidentId("MERCHANT", "mch_1"));
    }

    @Test
    @DisplayName("物业公司/平台不走 openid：PROPERTY 与 PLATFORM 一律解析不出住户")
    void resolveResidentId_nonPersonalTypes() {
        assertFalse(resolver.supportsPersonal("PROPERTY"));
        assertFalse(resolver.supportsPersonal("PLATFORM"));
        assertNull(resolver.resolveResidentId("PROPERTY", "pc_demo001"));
        assertNull(resolver.resolveResidentId("PLATFORM", "PLATFORM"));
    }

    @Test
    @DisplayName("主体记录缺失 / 未指向住户 → null，调用方据此报「主体不存在或未关联住户账号」")
    void resolveResidentId_missingPrincipal() {
        when(courierManagerRepository.findById("cm_missing")).thenReturn(Optional.empty());
        assertNull(resolver.resolveResidentId("COURIER", "cm_missing"));

        Merchant orphan = new Merchant();
        when(merchantRepository.findById("mch_orphan")).thenReturn(Optional.of(orphan));
        assertNull(resolver.resolveResidentId("MERCHANT", "mch_orphan"));
    }

    @Test
    @DisplayName("loadResident 住户记录不存在时返回 null")
    void loadResident_missingResidentRow() {
        when(residentRepository.findById("res_gone")).thenReturn(Optional.empty());
        assertNull(resolver.loadResident("RESIDENT", "res_gone"));

        Resident resident = new Resident();
        resident.setId("res_ok");
        when(residentRepository.findById("res_ok")).thenReturn(Optional.of(resident));
        assertTrue(resident.getId().equals(resolver.loadResident("RESIDENT", "res_ok").getId()));
    }
}
