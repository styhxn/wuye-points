package com.wuye.points.service;

import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 退款/取消冲正幂等测试：已冲正订单不得二次生成负数台账，也不得重复冲正钱包流水。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DistributionReverseIdempotencyTest {

    private static final String ORDER_ID = "ord_001";

    @Mock
    private DistributionRecordRepository distributionRecordRepository;
    @Mock
    private WalletFlowService walletFlowService;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private DistributionService service;

    @Test
    @DisplayName("首次冲正：生成一条负数台账并冲正全主体钱包流水")
    void firstReverse_createsReversal() {
        when(idGenerator.generateId("dr")).thenReturn("dr_rev_001");
        when(distributionRecordRepository.findByOrderId(ORDER_ID))
                .thenReturn(List.of(record("dr_001", "10.00"), record("dr_002", "2.00")));

        service.reverseForCancelledOrder(ORDER_ID);

        verify(distributionRecordRepository).save(any(DistributionRecord.class));
        verify(walletFlowService).recordReversal(anyString(), any(DistributionRecord.class));
    }

    @Test
    @DisplayName("台账已有负数冲正行：重复退款审核/回调重试不再二次冲正")
    void alreadyReversed_isIdempotent() {
        when(distributionRecordRepository.findByOrderId(ORDER_ID))
                .thenReturn(List.of(record("dr_002", "2.00"), record("dr_rev_001", "-10.00")));

        service.reverseForCancelledOrder(ORDER_ID);

        verify(distributionRecordRepository, never()).save(any());
        verify(walletFlowService, never()).recordReversal(anyString(), any());
    }

    @Test
    @DisplayName("无分成台账：直接返回，不写任何东西")
    void noRecords_noop() {
        when(distributionRecordRepository.findByOrderId(ORDER_ID)).thenReturn(List.of());

        service.reverseForCancelledOrder(ORDER_ID);

        verify(distributionRecordRepository, never()).save(any());
        verify(walletFlowService, never()).recordReversal(anyString(), any());
    }

    private static DistributionRecord record(String id, String distributableAmount) {
        DistributionRecord r = new DistributionRecord();
        r.setId(id);
        r.setOrderId(ORDER_ID);
        r.setMerchantId("mch_001");
        r.setPropertyCompanyId("pc_001");
        r.setResidentId("res_001");
        r.setCommissionRate(BigDecimal.ZERO);
        r.setTotalAmount(new BigDecimal("20.00"));
        r.setDistributableAmount(new BigDecimal(distributableAmount));
        r.setPropertyShare(BigDecimal.ZERO);
        r.setCoordinatorShare(BigDecimal.ZERO);
        return r;
    }
}
