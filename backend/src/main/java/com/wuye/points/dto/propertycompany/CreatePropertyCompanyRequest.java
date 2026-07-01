package com.wuye.points.dto.propertycompany;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreatePropertyCompanyRequest {

    @NotBlank(message = "物业公司名称不能为空")
    private String name;

    private String logoUrl;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String address;

    @NotEmpty(message = "至少需要创建一个小区")
    @Valid
    private List<CreateCommunityRequest> communities;

    @NotNull(message = "adminAccount不能为空")
    @Valid
    private CreateAdminAccountRequest adminAccount;

    @Data
    public static class CreateCommunityRequest {

        @NotBlank(message = "小区名称不能为空")
        private String name;

        private String address;
        private Integer totalBuildings;
        private Integer totalUnits;
    }

    @Data
    public static class CreateAdminAccountRequest {

        @NotBlank(message = "管理员姓名不能为空")
        private String name;

        @NotBlank(message = "管理员手机号不能为空")
        private String phone;

        private String email;
    }
}
