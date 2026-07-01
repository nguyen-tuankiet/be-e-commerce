package com.example.becommerce.dto.response.technician;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusySlotResponse {
    private String orderCode;     // Mã đơn hàng (Ẩn với khách khác)
    private LocalDateTime scheduledAt;  // Thời gian thợ hẹn khách làm
    private LocalDateTime expectedTime; // Thời gian dự kiến khách yêu cầu
    private String deviceName;    // Tên thiết bị sửa chữa (Ẩn với khách khác)
    private String address;       // Địa chỉ thi công (Ẩn với khách khác)
    private String status;        // Trạng thái đơn (Ẩn với khách khác)
}
