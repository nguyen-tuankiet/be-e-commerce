package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ExtraCostRequest {

    @Positive(message = "Chi phí phát sinh phải lớn hơn 0")
    private BigDecimal extraCost;

    @NotBlank(message = "Lý do phát sinh chi phí không được để trống")
    private String reason;
}
