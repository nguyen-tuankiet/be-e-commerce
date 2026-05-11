package com.example.becommerce.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CategoryUpsertRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 120, message = "Tiêu đề không vượt quá 120 ký tự")
    private String title;

    @Size(max = 2000, message = "Mô tả không vượt quá 2000 ký tự")
    private String description;

    private String priority = "normal";

    private String status = "active";

    private MultipartFile icon;
}
