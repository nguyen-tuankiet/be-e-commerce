package com.example.becommerce.dto.request;

import com.example.becommerce.entity.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Reusable request shape for internal notification creation flows.
 */
@Getter
@Setter
public class NotificationCreateRequest {

    @NotNull(message = "Loại thông báo là bắt buộc")
    private NotificationType type;

    @NotBlank(message = "Tiêu đề là bắt buộc")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung là bắt buộc")
    @Size(max = 1000, message = "Nội dung không được vượt quá 1000 ký tự")
    private String body;

    private Map<String, Object> data;
}

