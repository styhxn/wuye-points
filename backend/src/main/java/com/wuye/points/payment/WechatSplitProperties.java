package com.wuye.points.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信支付分账（普通分账 / profit sharing）业务配置，绑定 application.yml 的 {@code wechat-split} 前缀。
 *
 * <p>仅包含分账业务参数（开关、售后窗口、灰度白名单、自动提现、归集户监控阈值等），
 * 不含任何第三方聚合支付（扫呗 / LCSW / CBK）配置。真正的微信接口参数在 {@link WechatPayProperties}。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat-split")
public class WechatSplitProperties {

    /** 是否启用微信分账（默认 true）。 */
    private boolean enabled = true;

    /** 配送员是否改走微信冻结（送达触发冻结，不再入平台内部钱包）。 */
    private boolean courierSplitEnabled = true;

    /** 是否允许用户主动发起订单退款。 */
    private boolean userRefundEnabled = false;

    /** Path B 自动提现开关占位（普通分账下收款方在自身微信商户平台提现）。 */
    private boolean autoWithdrawEnabled = true;

    /** 归集户余额监控阈值（元），留空=不监控。 */
    private BigDecimal collectBalanceAlertThreshold;

    /** 灰度白名单（物业公司 ID 列表）；空=全部启用（受全局 enabled 约束）。 */
    private List<String> enabledPropertyCompanies = new ArrayList<>();

    /** 分账异步回调地址。 */
    private String notifyUrl;

    /** 提现异步回调地址（普通分账无平台代提，占位）。 */
    private String notifyUrlWithdraw;

    /** 售后窗口天数（超过则自动完结划账）。 */
    private int afterSaleDays = 7;

    /**
     * 该物业公司是否启用分账：全局关闭则 false；白名单非空时仅名单内启用。
     */
    public boolean isEnabledForProperty(String propertyCompanyId) {
        if (!enabled) {
            return false;
        }
        if (enabledPropertyCompanies == null || enabledPropertyCompanies.isEmpty()) {
            return true;
        }
        return propertyCompanyId != null && enabledPropertyCompanies.contains(propertyCompanyId);
    }
}
