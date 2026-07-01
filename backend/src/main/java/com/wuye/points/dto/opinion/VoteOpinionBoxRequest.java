package com.wuye.points.dto.opinion;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VoteOpinionBoxRequest {

    @NotBlank(message = "voteOption不能为空")
    private String voteOption;
}
