package com.wuye.points.payment;

import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.service.ActivityGroupService;
import com.wuye.points.service.CanteenService;
import com.wuye.points.service.CommunityServiceService;
import com.wuye.points.service.OrderService;
import com.wuye.points.service.PropertyCoinService;
import com.wuye.points.service.PropertyFeeService;
import com.wuye.points.service.ResidentShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 按业务类型分发“完成渠道支付”后的业务落地逻辑（扣积分/物业币、置订单已支付、返奖励、建配送单、算分账等）。
 *
 * <p>各业务服务内部均保证幂等（已 paid 则直接返回），因此本分发器可被回调、事件、调度器多次安全调用。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentBizDispatcher {

    private final OrderService orderService;
    private final PropertyFeeService propertyFeeService;
    private final PropertyCoinService propertyCoinService;
    private final CommunityServiceService communityServiceService;
    private final ActivityGroupService activityGroupService;
    private final ResidentShopService residentShopService;
    private final CanteenService canteenService;

    public void complete(PaymentRecord record) {
        switch (record.getBizType()) {
            case PaymentBizType.ORDER -> orderService.completeChannelPayment(record);
            case PaymentBizType.PROPERTY_FEE -> propertyFeeService.completeChannelPayment(record);
            case PaymentBizType.COIN_RECHARGE -> propertyCoinService.completeChannelPayment(record);
            case PaymentBizType.SERVICE_ORDER -> communityServiceService.completeBookChannelPayment(record);
            case PaymentBizType.SERVICE_SUBSCRIPTION -> communityServiceService.completeSubscribeChannelPayment(record);
            case PaymentBizType.ACTIVITY_SUBSCRIPTION -> activityGroupService.completeChannelPayment(record);
            case PaymentBizType.RESIDENT_SHOP_DEPOSIT -> residentShopService.completeChannelPayment(record);
            case PaymentBizType.QUOTA_PURCHASE -> canteenService.completeQuotaPurchase(record);
            default -> throw new BizException(ErrorCode.WECHAT_API_ERROR,
                    "未知支付业务类型: " + record.getBizType());
        }
    }
}
