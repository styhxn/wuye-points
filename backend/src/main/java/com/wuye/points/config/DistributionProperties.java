package com.wuye.points.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 分账策略配置。
 */
@Component
@ConfigurationProperties(prefix = "wuye.distribution")
@Data
public class DistributionProperties {

    /**
     * 快递员分成是否改由配送费核算（王总已确认，默认开启 B 方案）。
     * <ul>
     *   <li>true（默认）：快递员不再从平台盘分成，其收入来自配送费（Delivery.courier_earning）；
     *       平台盘仅从商品价格（不含配送费）计提，并在 平台服务费/物业/统筹/板块负责人/个体负责人 之间分账；
     *       平台盘三档变为 平台服务费/物业/管理人员，之和需为 100%。</li>
     *   <li>false：维持原「平台盘四档含快递 15%」逻辑（A 方案），钳制 0.6~5 元。</li>
     * </ul>
     */
    private boolean courierShareFromDeliveryFee = true;
}
