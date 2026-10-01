package com.wuye.points.dto.admin.roleaccount;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端直接创建业务角色账号请求（商家 / 活动组长 / 技工 / 配送员）。
 * 权限：物业管理员（含物业领导/物业操作员）、平台管理员。
 */
@Data
@Schema(description = "直接创建业务角色账号请求")
public class CreateRoleAccountRequest {

    @Schema(description = "角色：merchant=商家, activity_leader=活动组长, technician=技工, courier=配送员",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"merchant", "activity_leader", "technician", "courier"})
    @NotBlank(message = "角色不能为空")
    private String role;

    @Schema(description = "姓名", example = "张老板", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;

    @Schema(description = "手机号（登录账号）", example = "13900000008", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "登录密码（管理端登录用，BCrypt加密存储）", example = "admin123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6-32之间")
    private String password;

    @Schema(description = "物业公司ID（平台管理员必填；物业管理员可不传，自动使用当前物业）", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "小区ID（可选）", example = "com_demo001")
    private String communityId;

    // ===== 商家专属字段（role=merchant 时使用） =====

    @Schema(description = "商家品类（role=merchant 必填），如：外卖/周围商家/自营/农家/外卖小吃/团购", example = "周围商家")
    @Size(max = 50, message = "品类长度不能超过50")
    private String category;

    @Schema(description = "商家描述（可选）")
    private String description;

    @Schema(description = "营业时间（可选）", example = "09:00-22:00")
    @Size(max = 50, message = "营业时间长度不能超过50")
    private String businessHours;

    @Schema(description = "商家地址（可选）")
    @Size(max = 200, message = "地址长度不能超过200")
    private String address;

    @Schema(description = "封面图URL列表（可选）")
    private List<String> coverUrls;

    @Schema(description = "配送费（可选，默认0）", example = "0.00")
    private BigDecimal deliveryFee;

    @Schema(description = "抽佣比例（可选，默认0.1000=10%）", example = "0.1000")
    private BigDecimal commissionRate;

    @Schema(description = "积分兑换比（可选，默认100，即100积分=1元）", example = "100")
    private BigDecimal pointExchangeRate;
}
