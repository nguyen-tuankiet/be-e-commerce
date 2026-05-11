package com.example.becommerce.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAdjustmentRequest {

    @NotNull(message = "Giá mới không được trống")
    @Positive(message = "Giá mới phải > 0")
    private Long newPrice;

    @NotBlank(message = "Lý do điều chỉnh giá không được trống")
    private String reason;

    private List<PartItem> parts;

    private List<String> evidenceImages;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartItem {
        @NotBlank
        private String name;

        @NotNull
        @Positive
        private Long price;

        private String partCode;
    }
}
