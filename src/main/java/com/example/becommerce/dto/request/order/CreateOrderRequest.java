package com.example.becommerce.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
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
public class CreateOrderRequest {

    @NotBlank(message = "Tên thiết bị không được trống")
    private String deviceName;

    @NotBlank(message = "Mô tả sự cố không được trống")
    private String description;

    @NotBlank(message = "Địa chỉ không được trống")
    private String address;

    @PositiveOrZero(message = "Giá ước tính không hợp lệ")
    private Long estimatedPrice;

    private LocalDateTime expectedTime;

    private String serviceCategory;

    private String serviceName;

    private String subService;

    private List<String> images;
}
