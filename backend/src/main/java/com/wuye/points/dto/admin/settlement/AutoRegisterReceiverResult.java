package com.wuye.points.dto.admin.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 单个主体的自动登记结果。
 *
 * <p>批量登记逐条返回，不因个别人缺 openid 而整批失败：{@code status} 取
 * {@code registered}（已写入接收方）/ {@code skipped}（主体没认领微信身份等原因，未写入）。</p>
 */
@Data
@Builder
@Schema(description = "分账个人接收方自动登记结果")
public class AutoRegisterReceiverResult {

    @Schema(description = "主体ID", example = "cm_001")
    private String ownerId;

    @Schema(description = "主体姓名", example = "李四")
    private String name;

    @Schema(description = "结果：registered=已登记 / skipped=未登记", example = "registered")
    private String status;

    @Schema(description = "openid 脱敏值（登记成功时回显，供核对是否本人）", example = "oUpF****eS6o")
    private String openidMasked;

    @Schema(description = "接收方账户ID（status=registered 时有值）")
    private String receiverAccountId;

    @Schema(description = "未登记原因 / 登记备注", example = "该账号未认领微信身份（无 openid），请本人在小程序登录一次")
    private String reason;
}
