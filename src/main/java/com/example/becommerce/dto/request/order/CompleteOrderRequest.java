package com.example.becommerce.dto.request.order;

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
public class CompleteOrderRequest {

    @Positive(message = "Giá phải lớn hơn 0")
    private Long finalPrice;

    private List<String> images;
}
