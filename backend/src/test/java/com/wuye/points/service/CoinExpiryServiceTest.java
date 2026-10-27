package com.wuye.points.service;

import com.wuye.points.entity.Family;
import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PropertyCoinRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.CoinExpiryRecordRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.PointPoolRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 物业币过期去重回归（A3 后续）。
 *
 * <p>核心验证：同一笔 earn 流水的 expired_amount 只会增加一次，不会在下一轮任务中重复扣减。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CoinExpiryServiceTest {

    @Mock
    private PropertyCoinRecordRepository propertyCoinRecordRepository;
    @Mock
    private CoinExpiryRecordRepository coinExpiryRecordRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private PointPoolRepository pointPoolRepository;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private CoinExpiryService coinExpiryService;

    private PropertyCompany company;
    private PointPool pool;

    @BeforeEach
    void setUp() {
        company = new PropertyCompany();
        company.setId("pc1");
        company.setCoinExpiryDays(365);

        pool = new PointPool();
        pool.setId("pp1");
        pool.setPropertyCompanyId("pc1");
        pool.setBalance(BigDecimal.ZERO);
        pool.setTotalIn(BigDecimal.ZERO);
        pool.setTotalOut(BigDecimal.ZERO);

        when(idGenerator.generateId(anyString())).thenAnswer(inv -> inv.getArgument(0) + "_1");
        when(propertyCoinRecordRepository.save(any(PropertyCoinRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(pointPoolRepository.findByPropertyCompanyIdForUpdate("pc1")).thenReturn(Optional.of(pool));
    }

    @Test
    @DisplayName("同一笔流水第二次不会被重复清零")
    void expire_recordsAreNotRepeated() {
        Resident resident = resident("res1", null, new BigDecimal("100"));
        when(residentRepository.findByPropertyCompanyId("pc1")).thenReturn(List.of(resident));
        when(residentRepository.findByIdForUpdate("res1")).thenReturn(Optional.of(resident));

        PropertyCoinRecord earn80 = record("c1", "res1", null, new BigDecimal("80"));
        PropertyCoinRecord earn50 = record("c2", "res1", null, new BigDecimal("50"));
        when(propertyCoinRecordRepository.findExpirablePersonal(eq("res1"), any()))
                .thenReturn(List.of(earn80, earn50));

        CoinExpiryService.ExpiryResult first =
                coinExpiryService.expireForCompany(company, LocalDate.now(), null, false, "system");

        assertEquals(1, first.processedCount());
        assertEquals(0, new BigDecimal("100").compareTo(first.totalExpired()));
        assertEquals(0, BigDecimal.ZERO.compareTo(resident.getCoinBalance()));
        assertEquals(0, new BigDecimal("80").compareTo(earn80.getExpiredAmount()));
        assertEquals(0, new BigDecimal("20").compareTo(earn50.getExpiredAmount()));
        assertEquals(0, new BigDecimal("100").compareTo(pool.getBalance()));
        verify(coinExpiryRecordRepository).save(any());

        // 模拟用户后续又获得 10 个币；旧流水只剩 30 未过期，本次最多再扣 10，而不是重扣 130。
        resident.setCoinBalance(new BigDecimal("10"));
        CoinExpiryService.ExpiryResult second =
                coinExpiryService.expireForCompany(company, LocalDate.now(), null, false, "system");

        assertEquals(1, second.processedCount());
        assertEquals(0, new BigDecimal("10").compareTo(second.totalExpired()));
        assertEquals(0, new BigDecimal("30").compareTo(earn50.getExpiredAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(resident.getCoinBalance()));
        assertEquals(0, new BigDecimal("110").compareTo(pool.getBalance()));
    }

    @Test
    @DisplayName("家庭池过期：扣家庭余额并写入 owner 的过期记录")
    void expire_familyPool() {
        Resident owner = resident("res_owner", "fam1", BigDecimal.ZERO);
        Resident member = resident("res_member", "fam1", BigDecimal.ZERO);
        when(residentRepository.findByPropertyCompanyId("pc1")).thenReturn(List.of(owner, member));

        Family family = new Family();
        family.setId("fam1");
        family.setOwnerId("res_owner");
        family.setPropertyCompanyId("pc1");
        family.setCoinBalance(new BigDecimal("100"));
        when(familyRepository.findByIdForUpdate("fam1")).thenReturn(Optional.of(family));

        PropertyCoinRecord record = record("c3", "res_owner", "fam1", new BigDecimal("100"));
        when(propertyCoinRecordRepository.findExpirableFamily(
                eq("fam1"), anyList(), any())).thenReturn(List.of(record));

        CoinExpiryService.ExpiryResult result =
                coinExpiryService.expireForCompany(company, LocalDate.now(), null, false, "system");

        assertEquals(1, result.processedCount());
        assertEquals(0, new BigDecimal("100").compareTo(result.totalExpired()));
        assertEquals(0, BigDecimal.ZERO.compareTo(family.getCoinBalance()));
        assertEquals(0, new BigDecimal("100").compareTo(record.getExpiredAmount()));
        verify(coinExpiryRecordRepository).save(any());
    }

    @Test
    @DisplayName("dryRun 只试算不写库")
    void expire_dryRunDoesNotWrite() {
        Resident resident = resident("res1", null, new BigDecimal("100"));
        when(residentRepository.findByPropertyCompanyId("pc1")).thenReturn(List.of(resident));
        when(residentRepository.findById("res1")).thenReturn(Optional.of(resident));

        PropertyCoinRecord record = record("c4", "res1", null, new BigDecimal("100"));
        when(propertyCoinRecordRepository.findExpirablePersonal(eq("res1"), any()))
                .thenReturn(List.of(record));

        CoinExpiryService.ExpiryResult result =
                coinExpiryService.expireForCompany(company, LocalDate.now(), null, true, "admin");

        assertEquals(1, result.processedCount());
        assertEquals(0, new BigDecimal("100").compareTo(result.totalExpired()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.clearedToPool()));
        assertEquals(0, BigDecimal.ZERO.compareTo(record.getExpiredAmount()));
        assertEquals(0, new BigDecimal("100").compareTo(resident.getCoinBalance()));
        verify(propertyCoinRecordRepository, never()).save(any());
        verify(coinExpiryRecordRepository, never()).save(any());
    }

    private Resident resident(String id, String familyId, BigDecimal balance) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setFamilyId(familyId);
        resident.setPropertyCompanyId("pc1");
        resident.setCoinBalance(balance);
        return resident;
    }

    private PropertyCoinRecord record(String id, String residentId, String familyId, BigDecimal amount) {
        PropertyCoinRecord record = new PropertyCoinRecord();
        record.setId(id);
        record.setResidentId(residentId);
        record.setFamilyId(familyId);
        record.setPropertyCompanyId("pc1");
        record.setCoinType("earn");
        record.setAmount(amount);
        record.setBalance(amount);
        record.setExpiredAmount(BigDecimal.ZERO);
        return record;
    }
}
