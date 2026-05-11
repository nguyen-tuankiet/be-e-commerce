package com.example.becommerce.dto.request.quotation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuotationRequest {

    @NotBlank(message = "Tên dịch vụ không được trống")
    private String serviceName;

    private String description;

    @NotNull(message = "Giá không được trống")
    @Positive(message = "Giá phải > 0")
    private Long price;

    private LocalDateTime scheduledAt;

    private String notes;
}
