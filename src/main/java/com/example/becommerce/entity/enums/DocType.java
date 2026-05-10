package com.example.becommerce.entity.enums;

/**
 * Loại tài liệu xác minh danh tính
 */
public enum DocType {
    ID_FRONT,      // Mặt trước CMND/Passport
    ID_BACK,       // Mặt sau CMND/Passport
    PORTRAIT,      // Ảnh chân dung
    CERTIFICATE,   // Chứng chỉ/diploma
    SELFIE,        // Ảnh selfie cầm giấy tờ
    OTHER          // Khác
}
