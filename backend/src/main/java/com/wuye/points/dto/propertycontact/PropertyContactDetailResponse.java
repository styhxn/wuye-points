package com.wuye.points.dto.propertycontact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "物业联系方式配置详情（管理端）")
public class PropertyContactDetailResponse {

    @Schema(description = "配置ID", example = "pcc_1a2b3c4d5e6f")
    private String id;

    @Schema(description = "物业名称", example = "阳光花园物业服务中心")
    private String propertyName;

    @Schema(description = "联系电话", example = "0571-88886666")
    private String contactPhone;

    @Schema(description = "备用手机/微信电话", example = "13800000000")
    private String contactMobile;

    @Schema(description = "物业地址", example = "浙江省杭州市西湖区阳光路1号")
    private String address;

    @Schema(description = "服务时间", example = "周一至周日 8:00-20:00")
    private String serviceHours;

    @Schema(description = "联系邮箱", example = "service@sunshine-property.com")
    private String email;

    @Schema(description = "备注（仅管理端可见）", example = "前台分机 8001")
    private String remark;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
