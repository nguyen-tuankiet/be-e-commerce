package com.example.becommerce.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CreateReviewRequest {

    @NotNull(message = "Đánh giá không được trống")
    @Min(value = 1, message = "Đánh giá phải >= 1")
    @Max(value = 5, message = "Đánh giá phải <= 5")
    private Integer rating;

    private String content;

    private List<String> attachedImages;
}
