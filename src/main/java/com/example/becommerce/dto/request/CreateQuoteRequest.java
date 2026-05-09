package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQuoteRequest {

    @NotNull(message = "Số tiền báo giá không được để trống")
    @Positive(message = "Số tiền báo giá phải lớn hơn 0")
    private Long amount;

    @NotBlank(message = "Mô tả báo giá không được để trống")
    private String description;
}
