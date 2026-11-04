package com.wuye.points.controller;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.service.DistributionService;
import com.wuye.points.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用户侧两档（积分 / 奖励金）端到端验证（V36）。
 *
 * <p>覆盖两个缺陷：</p>
 * <ol>
 *   <li>含现金支付的订单在微信回调 {@code completeChannelPayment} 里只扣减用户使用的积分/物业币，
 *       从未调用发放，导致真实付费订单 {@code point_earned} 恒为 0。此前无任何测试触及该字段，
 *       所以缺陷长期未被发现。</li>
 *   <li>发放口径从「按用户实付金额」改为「按抽佣基础额 × 物业公司预算比例」，
 *       并且这两档要在分账链上先扣掉，物业/统筹按扣完的分。</li>
 * </ol>
 */
@DisplayName("用户侧积分/奖励金先扣档与现金支付发放")
class OrderUserIncentiveFlowTest extends ControllerTestBase {

    private static final String PC = "pc_inc001";
    private static final String RES = "res_inc001";
    private static final String MCH = "mch_inc001";
    private static final String MPC = "mpc_inc001";
    private static final String ORD = "ord_inc001";

    @Autowired
    private OrderService orderService;
    @Autowired
    private DistributionService distributionService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ResidentRepository residentRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Autowired
    private PropertyCompanyRepository propertyCompanyRepository;
    @Autowired
    private DistributionRecordRepository distributionRecordRepository;

    @BeforeEach
    void seed() {
        distributionRecordRepository.deleteAll();
        orderRepository.deleteAll();
        merchantPropertyConfigRepository.deleteAll();
        merchantRepository.deleteAll();
        residentRepository.deleteAll();
        propertyCompanyRepository.deleteAll();

        // 一级三档按老板逻辑图：平台不留成，物业 50% / 统筹 50%
        saveCompany(new BigDecimal("0.1000"), new BigDecimal("0.1000"),
                BigDecimal.ZERO, new BigDecimal("0.5000"), new BigDecimal("0.5000"));

        Resident resident = new Resident();
        resident.setId(RES);
        resident.setName("测试业主");
        resident.setPhone("13910000001");
        resident.setPropertyCompanyId(PC);
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        residentRepository.save(resident);

        Merchant merchant = new Merchant();
        merchant.setId(MCH);
        merchant.setPlatformMerchantId("pm_inc001");
        merchant.setPropertyCompanyId(PC);
        merchant.setName("测试商家");
        merchant.setCategory("外卖小吃");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setMerchantSource("platform");
        merchant.setDeliveryFee(BigDecimal.ZERO);
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantRepository.save(merchant);

        saveMerchantConfig(new BigDecimal("0.0500"));
    }

    private void saveCompany(BigDecimal userPointRate, BigDecimal userRewardRate,
                             BigDecimal platformRate, BigDecimal propertyRate, BigDecimal coordinatorRate) {
        PropertyCompany company = new PropertyCompany();
        company.setId(PC);
        company.setName("测试物业公司");
        company.setStatus("active");
        company.setUserPointShareRate(userPointRate);
        company.setUserRewardShareRate(userRewardRate);
        company.setPlatformShareRate(platformRate);
        company.setPropertyShareRate(propertyRate);
        company.setCoordinatorShareRate(coordinatorRate);
        company.setSectorLeaderRate(new BigDecimal("0.5000"));
        company.setIndividualLeaderRate(new BigDecimal("0.6000"));
        propertyCompanyRepository.save(company);
    }

    private void saveMerchantConfig(BigDecimal commissionRate) {
        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(MPC);
        config.setMerchantId(MCH);
        config.setPropertyCompanyId(PC);
        config.setCommissionRate(commissionRate);
        config.setPointExchangeRate(new BigDecimal("100.0000"));
        // 商家级返现开关/比例自 V36 起不再参与订单返现，这里故意置空以证明不再被读取
        config.setCoinRebateEnabled(false);
        config.setCoinRebateRate(BigDecimal.ZERO);
        config.setStatus("active");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        merchantPropertyConfigRepository.save(config);
    }

    private Order savePendingOrder(String totalAmount, String couponAmount) {
        Order order = new Order();
        order.setId(ORD);
        order.setOrderNo("NO_INC_001");
        order.setResidentId(RES);
        order.setMerchantId(MCH);
        order.setPropertyCompanyId(PC);
        order.setOrderStatus("pending");
        order.setTotalAmount(new BigDecimal(totalAmount));
        order.setCouponAmount(new BigDecimal(couponAmount));
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setPointUsed(0);
        order.setCoinUsed(BigDecimal.ZERO);
        order.setCashAmount(new BigDecimal(totalAmount));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private PaymentRecord cashRecord(String cashAmount) {
        PaymentRecord record = new PaymentRecord();
        record.setId("pay_inc001");
        record.setBizType("ORDER");
        record.setBizId(ORD);
        record.setResidentId(RES);
        record.setPropertyCompanyId(PC);
        record.setPaymentMethod("wechat");
        record.setAmount(new BigDecimal(cashAmount));
        record.setCashAmount(new BigDecimal(cashAmount));
        record.setPointUsed(0);
        record.setCoinUsed(BigDecimal.ZERO);
        record.setStatus("paid");
        return record;
    }

    @Test
    @DisplayName("现金支付回调：按抽佣基础额×比例发放，且订单状态正常落账")
    void completeChannelPayment_grantsUserIncentives() {
        savePendingOrder("100.00", "0.00");

        orderService.completeChannelPayment(cashRecord("100.00"));

        Order paid = orderRepository.findById(ORD).orElseThrow();
        assertEquals("paid", paid.getOrderStatus());
        // 抽佣基础额 = 100 × 5% = 5.00；积分 = 5 × 10% × 100 = 50；奖励金 = 5 × 10% = 0.50
        assertEquals(50, paid.getPointEarned());
        assertEquals(0, new BigDecimal("0.50").compareTo(paid.getCoinEarned()));
        assertEquals(0, new BigDecimal("0.1000").compareTo(paid.getUserPointShareRate()));
        assertEquals(0, new BigDecimal("0.1000").compareTo(paid.getUserRewardShareRate()));

        Resident resident = residentRepository.findById(RES).orElseThrow();
        assertEquals(50, resident.getPointBalance());
        assertEquals(0, new BigDecimal("0.50").compareTo(resident.getCoinBalance()));
    }

    @Test
    @DisplayName("优惠券由商家承担：发放基数按扣券后的实付商品金额算")
    void completeChannelPayment_excludesCouponFromCommissionBase() {
        savePendingOrder("100.00", "20.00");

        orderService.completeChannelPayment(cashRecord("80.00"));

        Order paid = orderRepository.findById(ORD).orElseThrow();
        // 抽佣基础额 = (100 − 20) × 5% = 4.00 → 40 积分 / 0.40 元
        assertEquals(40, paid.getPointEarned());
        assertEquals(0, new BigDecimal("0.40").compareTo(paid.getCoinEarned()));
    }

    @Test
    @DisplayName("比例配 0 表示不发：不产生发放记录，订单照常完成")
    void completeChannelPayment_zeroRates_grantNothing() {
        saveCompany(BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, new BigDecimal("0.5000"), new BigDecimal("0.5000"));
        savePendingOrder("100.00", "0.00");

        orderService.completeChannelPayment(cashRecord("100.00"));

        Order paid = orderRepository.findById(ORD).orElseThrow();
        assertEquals("paid", paid.getOrderStatus());
        assertEquals(0, paid.getPointEarned());
        assertEquals(0, new BigDecimal("0.00").compareTo(paid.getCoinEarned()));
        assertEquals(0, residentRepository.findById(RES).orElseThrow().getPointBalance());
    }

    @Test
    @DisplayName("挂接配置缺失：回调内不抛异常（否则钱已收、订单永久 pending），按不发放处理")
    void completeChannelPayment_missingMerchantConfig_doesNotThrow() {
        merchantPropertyConfigRepository.deleteAll();
        savePendingOrder("100.00", "0.00");

        orderService.completeChannelPayment(cashRecord("100.00"));

        Order paid = orderRepository.findById(ORD).orElseThrow();
        assertEquals("paid", paid.getOrderStatus());
        assertEquals(0, paid.getPointEarned());
    }

    @Test
    @DisplayName("结算：用户两档从平台盘先扣，物业/统筹按扣完的分")
    void settlement_deductsUserTiersBeforePrimarySplit() {
        Order paid = savePendingOrder("100.00", "0.00");
        orderService.completeChannelPayment(cashRecord("100.00"));
        paid = orderRepository.findById(ORD).orElseThrow();
        Resident resident = residentRepository.findById(RES).orElseThrow();
        MerchantPropertyConfig config = merchantPropertyConfigRepository.findById(MPC).orElseThrow();

        DistributionRecord record = distributionService.calculateAndSave(paid, resident, config);

        assertNotNull(record);
        // 抽佣 5.00 − 积分兑付 0 − 用户积分 0.50 − 奖励金 0.50 = 可分配 4.00
        assertEquals(0, new BigDecimal("0.50").compareTo(record.getUserPointBudget()));
        assertEquals(0, new BigDecimal("0.50").compareTo(record.getUserRewardBudget()));
        assertEquals(0, new BigDecimal("4.00").compareTo(record.getDistributableAmount()));
        // 无统筹账号时，扣完用户两档后的全额归物业公司（平台比例配 0）
        assertEquals(0, new BigDecimal("0.00").compareTo(record.getPlatformShare()));
        assertEquals(0, new BigDecimal("4.00").compareTo(record.getPropertyShare()));
        assertEquals(0, new BigDecimal("0.00").compareTo(record.getCoordinatorShare()));
        // 守恒：用户两档 + 物业 + 统筹 = 抽佣基础额
        BigDecimal sum = record.getUserPointBudget().add(record.getUserRewardBudget())
                .add(record.getPlatformShare()).add(record.getPropertyShare())
                .add(record.getCoordinatorShare());
        assertEquals(0, new BigDecimal("5.00").compareTo(sum));
    }

    @Test
    @DisplayName("历史订单（无比例快照）按物业公司当前配置兜底 10%/10%")
    void settlement_fallsBackToCompanyRatesWhenOrderSnapshotMissing() {
        Order legacy = savePendingOrder("100.00", "0.00");
        // 模拟 V36 之前支付的老订单：没有比例快照
        legacy.setUserPointShareRate(null);
        legacy.setUserRewardShareRate(null);
        orderRepository.save(legacy);

        Resident resident = residentRepository.findById(RES).orElseThrow();
        MerchantPropertyConfig config = merchantPropertyConfigRepository.findById(MPC).orElseThrow();
        DistributionRecord record = distributionService.calculateAndSave(legacy, resident, config);

        assertEquals(0, new BigDecimal("0.1000").compareTo(record.getUserPointShareRate()));
        assertEquals(0, new BigDecimal("4.00").compareTo(record.getDistributableAmount()));
    }

    @Test
    @DisplayName("发放额与结算扣档额同源：两者必须相等，否则平台盘凭空多扣或少扣")
    void grantedAmountMatchesBudgetDeductedInSettlement() {
        savePendingOrder("100.00", "0.00");
        orderService.completeChannelPayment(cashRecord("100.00"));

        Order paid = orderRepository.findById(ORD).orElseThrow();
        Resident resident = residentRepository.findById(RES).orElseThrow();
        MerchantPropertyConfig config = merchantPropertyConfigRepository.findById(MPC).orElseThrow();
        DistributionRecord record = distributionService.calculateAndSave(paid, resident, config);

        // 发放的积分折算成元 == 结算从盘里扣的积分预算
        BigDecimal grantedPointValue = BigDecimal.valueOf(paid.getPointEarned())
                .divide(config.getPointExchangeRate(), 2, java.math.RoundingMode.HALF_UP);
        assertEquals(0, grantedPointValue.compareTo(record.getUserPointBudget()));
        assertEquals(0, paid.getCoinEarned().compareTo(record.getUserRewardBudget()));
        assertTrue(record.getUserPointBudget().signum() > 0);
    }
}
