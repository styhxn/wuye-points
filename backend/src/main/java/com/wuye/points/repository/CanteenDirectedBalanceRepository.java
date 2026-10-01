package com.wuye.points.repository;

import com.wuye.points.entity.CanteenDirectedBalance;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 社区食堂-用户定向余额账户 Repository
 */
@Repository
public interface CanteenDirectedBalanceRepository extends JpaRepository<CanteenDirectedBalance, String> {

    Optional<CanteenDirectedBalance> findByResidentIdAndMainMerchantId(String residentId, String mainMerchantId);

    List<CanteenDirectedBalance> findByResidentId(String residentId);

    List<CanteenDirectedBalance> findByResidentIdAndMainMerchantIdIn(String residentId, List<String> mainMerchantIds);

    /**
     * 悲观锁加载定向余额账户（SELECT ... FOR UPDATE）。
     *
     * <p>背景：实体虽带 {@code @Version} 乐观锁，但订单支付扣减是直接扣用户钱的关键路径。
     * 乐观锁在并发下会抛 {@code ObjectOptimisticLockingFailureException}，
     * 而此时<b>支付已经成功</b>——原实现直接把异常转成"请重试"抛给前端，
     * 结果就是「用户付款成功、定向余额却没扣」，可以反复白嫖。
     *
     * <p>改用行锁后，并发请求串行进入，后到者读到的是已扣减后的余额并据此判断，
     * 从根本上消除该类冲突；{@code @Version} 仍保留作为跨路径的兜底保护。
     *
     * <p>必须显式声明 @Query：否则 Spring Data 会按方法名派生查询，
     * 把 "ForUpdate" 当作属性路径解析而导致启动失败。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT b FROM CanteenDirectedBalance b "
            + "WHERE b.residentId = :residentId AND b.mainMerchantId = :mainMerchantId")
    Optional<CanteenDirectedBalance> findByResidentIdAndMainMerchantIdForUpdate(
            @Param("residentId") String residentId,
            @Param("mainMerchantId") String mainMerchantId);
}
