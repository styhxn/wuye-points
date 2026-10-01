package com.wuye.points.dto.propertyfee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "催缴语义内容（后端按住户欠费记录合并生成，用于渲染应用内文案与映射微信模板字段）")
public class ArrearsReminderContent {

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "物业公司名称", example = "万达物业")
    private String propertyName;

    @Schema(description = "小区名称", example = "盘锦花地小区")
    private String communityName;

    @Schema(description = "房屋地址（楼栋-单元-房号）", example = "3栋-2单元-1001")
    private String houseAddress;

    @Schema(description = "欠费账期（起始 至 结束）", example = "2026-01 至 2026-07")
    private String arrearsPeriod;

    @Schema(description = "欠费金额合计", example = "1280.50")
    private BigDecimal arrearsAmount;

    @Schema(description = "缴费截止日期", example = "2026-08-31")
    private String paymentDeadline;

    @Schema(description = "温馨提示文案", example = "如已缴费请忽略本通知")
    private String reminderText;
}
