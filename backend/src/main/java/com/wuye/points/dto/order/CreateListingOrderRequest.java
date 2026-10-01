package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "向业主商户下单（购买其上架商品）请求")
public class CreateListingOrderRequest {

    @NotBlank(message = "上架ID不能为空")
    @Schema(description = "业主商户上架记录ID", required = true, example = "rml_abc123")
    private String listingId;

    @Schema(description = "数量，默认1", example = "1")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    @Size(max = 200, message = "配送地址长度不能超过200")
    @Schema(description = "配送地址", example = "阳光花园1号楼1单元101")
    private String address;

    @Size(max = 20, message = "联系电话长度不能超过20")
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注", example = "请尽快配送")
    private String remark;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;
}
