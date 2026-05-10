package com.example.becommerce.repository;

import com.example.becommerce.entity.TechnicianProfile;
import com.example.becommerce.entity.enums.KycStatus;
import com.example.becommerce.entity.enums.TechnicianStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for TechnicianProfile entity.
 */
@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Long> {

    /**
     * Tìm hồ sơ thợ theo user ID
     */
    Optional<TechnicianProfile> findByUserId(Long userId);

    /**
     * Kiểm tra user đã có TechnicianProfile chưa
     */
    boolean existsByUserId(Long userId);

    /**
     * Lấy danh sách thợ đã được xác minh và hoạt động
     */
    @Query("SELECT tp FROM TechnicianProfile tp " +
            "WHERE tp.technicianStatus = 'ACTIVE' " +
            "AND tp.kycStatus = 'APPROVED' " +
            "AND tp.user.deleted = false")
    Page<TechnicianProfile> findActiveAndVerifiedTechnicians(Pageable pageable);

    /**
     * Lấy danh sách thợ sẵn sàng nhận đơn (available = true)
     */
    @Query("SELECT tp FROM TechnicianProfile tp " +
            "WHERE tp.technicianStatus = 'ACTIVE' " +
            "AND tp.kycStatus = 'APPROVED' " +
            "AND tp.available = true " +
            "AND tp.user.deleted = false")
    Page<TechnicianProfile> findAvailableTechnicians(Pageable pageable);

    /**
     * Lấy danh sách thợ theo trạng thái KYC
     */
    Page<TechnicianProfile> findByKycStatus(KycStatus kycStatus, Pageable pageable);

    /**
     * Lấy danh sách thợ theo trạng thái đăng ký
     */
    Page<TechnicianProfile> findByTechnicianStatus(TechnicianStatus status, Pageable pageable);

    /**
     * Đếm số thợ đã xác minh
     */
    long countByKycStatusAndTechnicianStatus(KycStatus kycStatus, TechnicianStatus status);
}
