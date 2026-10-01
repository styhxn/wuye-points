package com.wuye.points.service;

import com.wuye.points.entity.WalletFlow;
import com.wuye.points.repository.WalletFlowRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 钱包流水的隔离写入器。
 *
 * <p><b>为什么需要独立事务</b>：钱包流水靠 {@code idempotency_key} 唯一索引保证幂等。
 * 当两个请求并发结算同一笔业务时，先后的 {@code existsByIdempotencyKey} 检查可能都通过，
 * 后写入者触发唯一键冲突。若冲突发生在业务事务内，Hibernate 会把该事务标记为
 * rollback-only，即便代码捕获了 {@link DataIntegrityViolationException}，
 * 外层提交时仍会抛 {@code UnexpectedRollbackException} ——
 * 结果就是「整笔分账因幂等竞争而失败」，商家无法收到钱。
 *
 * <p>把单条流水的写入放到 {@code REQUIRES_NEW} 独立事务后：
 * <ul>
 *   <li>唯一键冲突只回滚这条 INSERT 所在的小事务，业务主事务不受影响；</li>
 *   <li>冲突即意味着"另一线程已写入"，语义上就是幂等命中，直接跳过即可。</li>
 * </ul>
 *
 * <p><b>权衡</b>：流水会比业务主事务先提交。若主事务随后回滚，流水需要靠冲正流水抵消。
 * 相比"并发竞争导致整笔分账失败"，这个代价更小且可观测（账本保留痕迹）。
 * 写入位置保持在业务流程的最后一步，可将该风险降到最低。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletFlowWriter {

    private final WalletFlowRepository walletFlowRepository;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 写入单条流水（独立事务）。
     *
     * @return true=本次实际写入；false=幂等跳过（已存在或并发冲突）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean write(String distributionRecordId, String orderId, String deliveryId,
                         String ownerType, String ownerId, String entryType,
                         BigDecimal amount, String balanceType,
                         String idempotencyKey, String refRecordId) {
        if (walletFlowRepository.existsByIdempotencyKey(idempotencyKey)) {
            return false;
        }
        WalletFlow flow = new WalletFlow();
        flow.setId(idGenerator.generateId("wf"));
        flow.setOwnerType(ownerType);
        flow.setOwnerId(ownerId);
        flow.setOrderId(orderId);
        flow.setDeliveryId(deliveryId);
        flow.setDistributionRecordId(distributionRecordId);
        flow.setEntryType(entryType);
        flow.setAmount(amount);
        flow.setBalanceType(balanceType);
        flow.setIdempotencyKey(idempotencyKey);
        flow.setRefRecordId(refRecordId);
        try {
            walletFlowRepository.saveAndFlush(flow);
            return true;
        } catch (DataIntegrityViolationException e) {
            // 并发下另一事务已写入同一幂等键，属预期内的幂等命中
            log.info("[钱包流水] 并发幂等命中，跳过重复入账 key={}", idempotencyKey);
            return false;
        }
    }
}
