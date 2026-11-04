package com.wuye.points.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 服务商「特约商户进件」配置（绑定 {@code wechat.applyment}）。
 *
 * <p>进件走普通服务商接口 {@code POST /v3/applyment4sub/applyment/}，凭据复用
 * {@code wechat.pay}（服务商 1752012787）。</p>
 *
 * <p>结算规则 ID 与行业名称均有官方固定映射（见 {@link PartnerApplymentRules}）：
 * 企业 716 / 个体户 719，默认行业「居民生活服务」；下方两项配置仅用于覆盖默认值，
 * 不是必填项。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.applyment")
public class PartnerApplymentProperties {

    /** 进件能力开关（关闭后商家端进件接口返回明确错误）。 */
    private boolean enabled = true;

    /** 资料保存后是否自动提交微信（失败仅记 lastError，可手动重试）。 */
    private boolean autoSubmit = false;

    /** 入驻结算规则ID 覆盖值；留空按主体类型自动取 716(企业)/719(个体户)。 */
    private String settlementId;

    /** 所属行业名称覆盖值；留空默认「居民生活服务」。 */
    private String qualificationType = PartnerApplymentRules.DEFAULT_INDUSTRY;

    /** 是否金融机构主体（默认 false）。 */
    private boolean financeInstitution = false;
}
