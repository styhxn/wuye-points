package com.wuye.points.payment;

import java.util.List;
import java.util.Set;

/**
 * 特约商户进件的「入驻结算规则」固定映射，来源于微信支付官方
 * 《入驻结算规则、行业属性及特殊资质》公示表（kf.qq.com/faq/220228IJb2UV220228uEjU3Q.html）：
 *
 * <ul>
 *   <li>结算规则 ID <b>716</b>：企业，费率 0.6%，T+1，覆盖餐饮/电商平台/零售/食品生鲜/
 *       咨询娱乐票务/房产中介/休闲娱乐旅游/居民生活服务/景区酒店/其他缴费/停车缴费；</li>
 *   <li>结算规则 ID <b>719</b>：个体工商户，费率 0.6%，T+1，行业范围同上；</li>
 *   <li>结算规则 ID <b>703</b>：小微（本平台不支持该主体类型）；</li>
 *   <li>结算规则 ID <b>717/721</b>：0.3% 专项（快递/物流/加油加气/民办学校），不在本平台受理范围。</li>
 * </ul>
 *
 * <p>因此商家只需要选择<b>行业名称</b>，结算规则 ID 由主体类型自动带出，不需要人工查表。</p>
 */
public final class PartnerApplymentRules {

    /** 默认行业：物业/社区服务（无需特殊资质）。 */
    public static final String DEFAULT_INDUSTRY = "居民生活服务";

    /** 平台受理的行业（企业 716 / 个体户 719）；顺序即前端下拉展示顺序。 */
    public static final List<String> SUPPORTED_INDUSTRIES = List.of(
            "居民生活服务", "餐饮", "零售", "食品生鲜", "电商平台", "咨询/娱乐票务",
            "房产中介", "休闲娱乐/旅游服务", "景区/酒店", "停车缴费", "其他缴费");

    /** 需上传特殊资质（收费授权证明/合同）才可提交的行业。 */
    public static final Set<String> NEEDS_QUALIFICATION = Set.of("其他缴费");

    /** 适用 0.3% 专项结算规则（717/721）的行业，本平台不代受理。 */
    public static final Set<String> SPECIAL_RULE_INDUSTRIES = Set.of(
            "快递", "物流", "加油/加气", "民办学校（非全国高等学校）");

    private PartnerApplymentRules() {
    }

    /** 按主体类型返回入驻结算规则 ID；不支持的主体返回 null。 */
    public static String settlementIdFor(String organizationType) {
        if ("ENTERPRISE".equals(organizationType)) {
            return "716";
        }
        if ("INDIVIDUAL".equals(organizationType)) {
            return "719";
        }
        return null;
    }
}
