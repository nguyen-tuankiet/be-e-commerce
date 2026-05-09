package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderCreateRequest {

    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String serviceName;

    private String description;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String district;

    @PositiveOrZero(message = "Giá tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal totalPrice;

    private String note;
}
