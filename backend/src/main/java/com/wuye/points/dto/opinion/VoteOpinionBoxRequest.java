package com.wuye.points.dto.opinion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "投票请求")
public class VoteOpinionBoxRequest {

    @NotBlank(message = "voteOption不能为空")
    @Schema(description = "投票选项", example = "support", allowableValues = {"support", "oppose", "abstain"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String voteOption;
}
