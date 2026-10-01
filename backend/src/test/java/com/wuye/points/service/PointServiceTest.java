package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.point.DeductFeeRequest;
import com.wuye.points.dto.point.EarnPointRequest;
import com.wuye.points.dto.point.SpendPointRequest;
import com.wuye.points.dto.point.TransferPointRequest;
import com.wuye.points.dto.resident.TransferToPropertyPointRequest;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyFee;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.service.PointConverter;
import com.wuye.points.util.FeatureSwitchGuard;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 积分核心逻辑单元测试（Mockito，不加载 Spring 上下文）。
 * 聚焦金额相关的高风险路径：积分转移、物业费抵扣、积分发放、积分消费，
 * 以及余额不足 / 越权 / 自身转账 / 未知来源等边界。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PointServiceTest {

    @Mock
    private PointRecordRepository pointRecordRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private PropertyFeeRepository propertyFeeRepository;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private PointConverter pointConverter;
    @Mock
    private FeatureSwitchGuard featureSwitchGuard;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    void setUp() {
        when(idGenerator.generateId(anyString())).thenReturn("pt_testid");
        // save 默认返回 null 会导致 createRecord 中的 record 变为 null，这里让其返回入参
        when(pointRecordRepository.save(any(PointRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        // 服务端并发控制使用悲观锁查询 findByIdForUpdate / findByIdForUpdate(Family)，
        // 测试委托到 findById 的 stub，避免每次用例重复 mock 加锁方法
        when(residentRepository.findByIdForUpdate(anyString()))
                .thenAnswer(inv -> residentRepository.findById(inv.getArgument(0)));
        when(familyRepository.findByIdForUpdate(anyString()))
                .thenAnswer(inv -> familyRepository.findById(inv.getArgument(0)));
        // void 类型 mock 默认 do-nothing，等于放行鉴权，便于专注测试积分逻辑
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private Resident resident(String id, String familyId, String pcId, int balance) {
        Resident r = new Resident();
        r.setId(id);
        r.setFamilyId(familyId);
        r.setPropertyCompanyId(pcId);
        r.setPointBalance(balance);
        return r;
    }

    private void asResident(String userId, String familyId, String pcId) {
        UserContext.set(UserContext.builder()
                .userId(userId)
                .role("resident")
                .propertyCompanyId(pcId)
                .familyId(familyId)
                .build());
    }

    // ---------------- 积分转移 ----------------

    @Test
    @DisplayName("transfer: 同家庭成员间正常转账，双方余额正确变更")
    void transfer_success() {
        asResident("res_1", "fam_1", "pc_1");
        Resident from = resident("res_1", "fam_1", "pc_1", 100);
        Resident to = resident("res_2", "fam_1", "pc_1", 50);
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(from));
        when(residentRepository.findById("res_2")).thenReturn(Optional.of(to));
        Family family = new Family();
        family.setId("fam_1");
        when(familyRepository.findById("fam_1")).thenReturn(Optional.of(family));

        TransferPointRequest req = new TransferPointRequest();
        req.setFamilyId("fam_1");
        req.setToMemberId("res_2");
        req.setAmount(30);
        req.setRemark("晚饭");

        pointService.transfer(req);

        assertEquals(70, from.getPointBalance());
        assertEquals(80, to.getPointBalance());
    }

    @Test
    @DisplayName("transfer: 余额不足应抛 POINT_INSUFFICIENT")
    void transfer_insufficient() {
        asResident("res_1", "fam_1", "pc_1");
        Resident from = resident("res_1", "fam_1", "pc_1", 100);
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(from));
        Family family = new Family();
        family.setId("fam_1");
        when(familyRepository.findById("fam_1")).thenReturn(Optional.of(family));
        when(residentRepository.findById("res_2")).thenReturn(Optional.of(resident("res_2", "fam_1", "pc_1", 50)));

        TransferPointRequest req = new TransferPointRequest();
        req.setFamilyId("fam_1");
        req.setToMemberId("res_2");
        req.setAmount(150);

        BizException ex = assertThrows(BizException.class, () -> pointService.transfer(req));
        assertEquals(ErrorCode.POINT_INSUFFICIENT.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("transfer: 转给自己应抛 PARAM_INVALID")
    void transfer_toSelf() {
        asResident("res_1", "fam_1", "pc_1");
        Resident from = resident("res_1", "fam_1", "pc_1", 100);
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(from));
        Family family = new Family();
        family.setId("fam_1");
        when(familyRepository.findById("fam_1")).thenReturn(Optional.of(family));

        TransferPointRequest req = new TransferPointRequest();
        req.setFamilyId("fam_1");
        req.setToMemberId("res_1");
        req.setAmount(10);

        assertThrows(BizException.class, () -> pointService.transfer(req));
    }

    @Test
    @DisplayName("transfer: 跨家庭转账应抛 PERMISSION_DENIED")
    void transfer_crossFamily() {
        asResident("res_1", "fam_1", "pc_1");
        Resident from = resident("res_1", "fam_1", "pc_1", 100);
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(from));
        // 转账按 id 字典序加载双方（悲观锁），收款方需存在但属于其它家庭
        when(residentRepository.findById("res_9")).thenReturn(Optional.of(resident("res_9", "fam_2", "pc_1", 10)));
        Family otherFamily = new Family();
        otherFamily.setId("fam_2");
        when(familyRepository.findById("fam_2")).thenReturn(Optional.of(otherFamily));

        TransferPointRequest req = new TransferPointRequest();
        req.setFamilyId("fam_2");
        req.setToMemberId("res_9");
        req.setAmount(10);

        BizException ex = assertThrows(BizException.class, () -> pointService.transfer(req));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    // ---------------- 积分发放 ----------------

    @Test
    @DisplayName("earn: 正常发放，居民与家庭余额增加")
    void earn_success() {
        UserContext.set(UserContext.builder().userId("op_1").role("property_admin").propertyCompanyId("pc_1").build());
        Resident r = resident("res_3", "fam_1", "pc_1", 10);
        when(residentRepository.findById("res_3")).thenReturn(Optional.of(r));
        Family family = new Family();
        family.setId("fam_1");
        family.setPointBalance(200);
        when(familyRepository.findById("fam_1")).thenReturn(Optional.of(family));

        EarnPointRequest req = new EarnPointRequest();
        req.setResidentId("res_3");
        req.setPointAmount(50);
        req.setSource("order");
        req.setSourceId("s1");
        req.setDescription("奖励");

        pointService.earn(req);

        assertEquals(60, r.getPointBalance());
        assertEquals(250, family.getPointBalance());
    }

    @Test
    @DisplayName("earn: 未知来源应抛 PARAM_INVALID")
    void earn_unknownSource() {
        UserContext.set(UserContext.builder().userId("op_1").role("property_admin").propertyCompanyId("pc_1").build());
        Resident r = resident("res_3", "fam_1", "pc_1", 10);
        when(residentRepository.findById("res_3")).thenReturn(Optional.of(r));

        EarnPointRequest req = new EarnPointRequest();
        req.setResidentId("res_3");
        req.setPointAmount(50);
        req.setSource("invalid_source");
        req.setSourceId("s1");
        req.setDescription("奖励");

        assertThrows(BizException.class, () -> pointService.earn(req));
    }

    // ---------------- 积分消费 ----------------

    @Test
    @DisplayName("spend: 正常消费，余额扣减")
    void spend_success() {
        UserContext.set(UserContext.builder().userId("op_1").role("resident").propertyCompanyId("pc_1").build());
        Resident r = resident("res_4", "fam_1", "pc_1", 20);
        when(residentRepository.findById("res_4")).thenReturn(Optional.of(r));

        SpendPointRequest req = new SpendPointRequest();
        req.setResidentId("res_4");
        req.setPointAmount(10);
        req.setSource("order");
        req.setSourceId("s1");
        req.setDescription("兑换");
        req.setUseFamilyPool(false);

        pointService.spend(req);

        assertEquals(10, r.getPointBalance());
    }

    @Test
    @DisplayName("spend: 余额不足应抛 POINT_INSUFFICIENT")
    void spend_insufficient() {
        UserContext.set(UserContext.builder().userId("op_1").role("resident").propertyCompanyId("pc_1").build());
        Resident r = resident("res_4", "fam_1", "pc_1", 20);
        when(residentRepository.findById("res_4")).thenReturn(Optional.of(r));

        SpendPointRequest req = new SpendPointRequest();
        req.setResidentId("res_4");
        req.setPointAmount(50);
        req.setSource("order");
        req.setSourceId("s1");
        req.setDescription("兑换");
        req.setUseFamilyPool(false);

        BizException ex = assertThrows(BizException.class, () -> pointService.spend(req));
        assertEquals(ErrorCode.POINT_INSUFFICIENT.getCode(), ex.getCode());
    }

    // ---------------- 转给物业 ----------------

    @Test
    @DisplayName("transferToProperty: 正常转出，个人余额扣减并记流水")
    void transferToProperty_success() {
        asResident("res_1", "fam_1", "pc_1");
        Resident r = resident("res_1", "fam_1", "pc_1", 100);
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(r));

        TransferToPropertyPointRequest req = new TransferToPropertyPointRequest();
        req.setPointAmount(30);
        req.setRemark("捐给物业");

        var resp = pointService.transferToProperty(req);

        assertEquals(70, r.getPointBalance());
        assertEquals(70, resp.getBalanceAfter());
        assertEquals(30, resp.getPointAmount());
    }

    @Test
    @DisplayName("transferToProperty: 余额不足应抛 POINT_INSUFFICIENT")
    void transferToProperty_insufficient() {
        asResident("res_1", "fam_1", "pc_1");
        Resident r = resident("res_1", "fam_1", "pc_1", 10);
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(r));

        TransferToPropertyPointRequest req = new TransferToPropertyPointRequest();
        req.setPointAmount(30);

        BizException ex = assertThrows(BizException.class, () -> pointService.transferToProperty(req));
        assertEquals(ErrorCode.POINT_INSUFFICIENT.getCode(), ex.getCode());
    }

    // ---------------- 物业费抵扣 ----------------

    @Test
    @DisplayName("deductFee: 正常抵扣，余额清零、费用部分支付")
    void deductFee_success() {
        asResident("res_1", "fam_1", "pc_1");
        Resident r = resident("res_1", "fam_1", "pc_1", 100);
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(r));
        when(propertyCompanyRepository.findById("pc_1")).thenReturn(Optional.empty()); // 走默认抵扣率 0.01

        PropertyFee fee = new PropertyFee();
        fee.setId("pf_1");
        fee.setResidentId("res_1");
        fee.setAmount(new BigDecimal("100"));
        fee.setPaidAmount(BigDecimal.ZERO);
        fee.setPaymentStatus("unpaid");
        when(propertyFeeRepository.findById("pf_1")).thenReturn(Optional.of(fee));

        DeductFeeRequest req = new DeductFeeRequest();
        req.setPropertyFeeId("pf_1");
        req.setPoints(100);

        pointService.deductFee(req);

        assertEquals(0, r.getPointBalance());
        assertEquals(0, new BigDecimal("1.00").compareTo(fee.getPaidAmount())); // 100 * 0.01
        assertEquals("partial", fee.getPaymentStatus());
    }

    @Test
    @DisplayName("deductFee: 积分余额不足应抛 POINT_INSUFFICIENT")
    void deductFee_insufficientPoints() {
        asResident("res_1", "fam_1", "pc_1");
        Resident r = resident("res_1", "fam_1", "pc_1", 50);
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(r));

        PropertyFee fee = new PropertyFee();
        fee.setId("pf_1");
        fee.setResidentId("res_1");
        fee.setAmount(new BigDecimal("100"));
        fee.setPaidAmount(BigDecimal.ZERO);
        fee.setPaymentStatus("unpaid");
        when(propertyFeeRepository.findById("pf_1")).thenReturn(Optional.of(fee));

        DeductFeeRequest req = new DeductFeeRequest();
        req.setPropertyFeeId("pf_1");
        req.setPoints(100);

        BizException ex = assertThrows(BizException.class, () -> pointService.deductFee(req));
        assertEquals(ErrorCode.POINT_INSUFFICIENT.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("deductFee: 费用已支付应抛 ORDER_STATUS_INVALID")
    void deductFee_alreadyPaid() {
        asResident("res_1", "fam_1", "pc_1");
        when(residentRepository.findById("res_1")).thenReturn(Optional.of(resident("res_1", "fam_1", "pc_1", 100)));

        PropertyFee fee = new PropertyFee();
        fee.setId("pf_1");
        fee.setResidentId("res_1");
        fee.setAmount(new BigDecimal("100"));
        fee.setPaidAmount(new BigDecimal("100"));
        fee.setPaymentStatus("paid");
        when(propertyFeeRepository.findById("pf_1")).thenReturn(Optional.of(fee));

        DeductFeeRequest req = new DeductFeeRequest();
        req.setPropertyFeeId("pf_1");
        req.setPoints(100);

        assertThrows(BizException.class, () -> pointService.deductFee(req));
    }
}
