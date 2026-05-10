package com.example.becommerce.entity.enums;

/**
 * Trạng thái bảo hành
 */
public enum WarrantyStatus {
    PENDING,        // Chờ xử lý
    IN_PROGRESS,    // Đang xử lý
    COMPLETED,      // Hoàn thành
    REJECTED,       // Bị từ chối
    EXPIRED         // Hết hạn bảo hành
}
