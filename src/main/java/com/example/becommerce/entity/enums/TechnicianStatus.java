package com.example.becommerce.entity.enums;

/**
 * Trạng thái đăng ký của thợ.
 * PENDING_KYC: Chưa xác minh danh tính
 * ACTIVE: Đã xác minh và hoạt động
 * SUSPENDED: Bị tạm khóa
 * REJECTED: Bị từ chối
 */
public enum TechnicianStatus {
    PENDING_KYC,
    ACTIVE,
    SUSPENDED,
    REJECTED
}
