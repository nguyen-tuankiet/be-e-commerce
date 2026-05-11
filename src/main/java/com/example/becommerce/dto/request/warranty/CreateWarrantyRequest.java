package com.example.becommerce.dto.request.warranty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWarrantyRequest {

    @NotBlank(message = "Mô tả lỗi không được trống")
    private String description;

    private List<String> images;

    private LocalDateTime scheduledAt;
}
