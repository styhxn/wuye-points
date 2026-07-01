package com.wuye.points.dto.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookServiceRequest {

    private String serviceTime;

    @NotBlank(message = "地址不能为空")
    private String address;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String remark;

    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
}
