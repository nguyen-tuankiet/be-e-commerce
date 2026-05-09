package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCancelRequest {

    @NotBlank(message = "Lý do hủy đơn không được để trống")
    private String cancelReason;
}
