package com.wuye.points.dto.directedmessage;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateAgeBracketsRequest {

    @NotEmpty
    @Valid
    @JsonAlias("brackets")
    private List<AgeBracketUpdateItem> items;

    @Data
    public static class AgeBracketUpdateItem {

        private String id;
        private String label;
        private Integer minAge;
        private Integer maxAge;
        private Integer sortOrder;
    }
}
