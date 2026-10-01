package com.wuye.points.dto.propertyfee;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "催缴通知请求")
public class ArrearsReminderRequest {

    @Schema(description = "物业公司ID（平台管理员可指定，物业管理员自动取当前物业）", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "社区ID，指定则只催缴该社区的欠费住户；留空表示全公司", example = "cm_demo001")
    private String communityId;

    @Schema(description = "楼栋，指定则只催缴该楼栋的欠费住户；留空表示不按楼栋过滤", example = "1栋")
    private String building;

    @Schema(description = "费用类型，指定则只催缴该类型欠费；留空表示全部", example = "property_fee")
    private String feeType;

    @Schema(description = "业务模板编码（前端只传编码，不传微信模板ID）", example = "property_fee_arrears_reminder_v1")
    private String templateCode = "property_fee_arrears_reminder_v1";

    @Schema(description = "预览接口返回的快照令牌，发送时校验以防范围漂移；为空则要求先预览", example = "prev_xxx")
    @JsonAlias("preview_token")
    private String previewToken;

    @Schema(description = "发送渠道，默认[\"in_app\",\"wechat\"]", example = "[\"in_app\",\"wechat\"]")
    private List<String> channels;

    @Schema(description = "幂等请求ID，重复提交同一 requestId 不会重复发送", example = "arrears_20260813_001")
    private String requestId;

    @Schema(description = "催缴标题（应用内消息标题）", example = "物业费催缴通知", required = true)
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100字")
    private String title;

    @Schema(description = "催缴内容（应用内消息正文 / 微信温馨提示）", example = "您有未缴纳的物业费，请尽快缴纳。", required = true)
    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容不能超过1000字")
    private String content;
}
