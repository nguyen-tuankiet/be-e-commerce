package com.example.becommerce.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}
