package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 最终受益人（UBO）资料。企业主体进件必填（官方 2026-04 起仅小微/个人卖家/个体户/
 * 政府机关/事业单位可免填）。
 *
 * <p>姓名、证件号码、居住地址为敏感字段，后端 RSA-OAEP 加密后随申请单提交。</p>
 */
@Schema(description = "最终受益人（UBO）资料")
@Data
public class UboInfoRequest {

    @Schema(description = "证件类型，默认 IDENTIFICATION_TYPE_IDCARD",
            example = "IDENTIFICATION_TYPE_IDCARD")
    private String idDocType;

    @Schema(description = "证件正面照片 media_id", required = true)
    @NotBlank(message = "受益人证件正面照片不能为空")
    private String idDocCopy;

    @Schema(description = "证件反面照片 media_id")
    private String idDocCopyBack;

    @Schema(description = "受益人姓名", required = true)
    @NotBlank(message = "受益人姓名不能为空")
    @Size(max = 100, message = "受益人姓名长度不能超过100")
    private String name;

    @Schema(description = "受益人证件号码", required = true)
    @NotBlank(message = "受益人证件号码不能为空")
    private String number;

    @Schema(description = "受益人居住地址", required = true)
    @NotBlank(message = "受益人居住地址不能为空")
    @Size(max = 512, message = "受益人居住地址长度不能超过512")
    private String address;

    @Schema(description = "证件有效期开始（YYYY-MM-DD）", required = true)
    @NotBlank(message = "受益人证件有效期开始不能为空")
    private String periodBegin;

    @Schema(description = "证件有效期结束（YYYY-MM-DD 或“长期”）", required = true)
    @NotBlank(message = "受益人证件有效期结束不能为空")
    private String periodEnd;
}
