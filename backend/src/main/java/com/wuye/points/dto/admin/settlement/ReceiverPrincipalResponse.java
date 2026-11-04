package com.wuye.points.dto.admin.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 分账个人接收方主体查询响应（管理端登记前预检）。
 *
 * <p>只回脱敏 openid（{@code oUpF****eS6o}）供人工核对是否本人，<b>不回明文 openid</b>：
 * openid 是打款地址，明文出接口就等于让运营手抄，抄错一位会静默打给另一个人。</p>
 */
@Data
@Builder
@Schema(description = "分账个人接收方主体预检响应")
public class ReceiverPrincipalResponse {

    @Schema(description = "主体类型", example = "COURIER")
    private String ownerType;

    @Schema(description = "主体ID（角色表主键，非 residents.id）", example = "cm_001")
    private String ownerId;

    @Schema(description = "主体姓名", example = "李四")
    private String name;

    @Schema(description = "手机号（脱敏）", example = "138****0008")
    private String phoneMasked;

    @Schema(description = "所属物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "是否可登记为个人收款方（已认领微信身份）", example = "true")
    private Boolean openidAvailable;

    @Schema(description = "openid 脱敏值，供核对是否本人；不可用于提交", example = "oUpF****eS6o")
    private String openidMasked;

    @Schema(description = "不可登记原因（可登记时为空）",
            example = "该账号未认领微信身份（无 openid），请本人在小程序登录一次")
    private String unavailableReason;

    @Schema(description = "是否已登记为分账接收方", example = "false")
    private Boolean registered;

    @Schema(description = "已登记时的接收方账户ID（可用于 PUT/DELETE）")
    private String receiverAccountId;
}
