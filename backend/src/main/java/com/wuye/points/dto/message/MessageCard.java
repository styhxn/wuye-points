package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 聊天消息卡片（实时解析，不存快照）
 * 卡片内容来自当前商品/订单数据；对象已删除时 title 显示「商品已下架/订单已失效」
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息卡片")
public class MessageCard {

    @Schema(description = "卡片类型 product/order/listing（橱窗商品）")
    private String cardType;

    @Schema(description = "卡片关联ID（商品ID或订单ID）")
    private String cardId;

    @Schema(description = "橱窗商品归属业主ID（resellerId），仅 listing 卡片返回，用于跳转到 shop/detail?listingId&resellerId")
    private String resellerId;

    @Schema(description = "卡片标题（商品名 / 订单号）")
    private String title;

    @Schema(description = "卡片图片URL")
    private String imageUrl;

    @Schema(description = "卡片金额（商品价 / 订单总额）")
    private BigDecimal price;

    @Schema(description = "对象状态（product: active/inactive/sold_out；order: 订单状态）")
    private String status;

    @Schema(description = "字段名形式摘要文本（如『商品名称：xxx\n商品价格：xx元』，前端可按文本渲染代替可视化卡片）")
    private String summary;
}
