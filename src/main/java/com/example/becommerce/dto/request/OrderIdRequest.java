package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderIdRequest {

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Long orderId;
}
