package com.example.becommerce.repository;

import com.example.becommerce.entity.Verification;
import com.example.becommerce.entity.enums.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {

    /**
     * Tìm verification theo mã
     */
    Optional<Verification> findByCode(String code);

    /**
     * Tìm verification gần nhất của thợ
     */
    Optional<Verification> findTopByTechnicianIdOrderByCreatedAtDesc(Long technicianId);

    /**
     * Lấy danh sách verification theo status
     */
    Page<Verification> findByStatus(KycStatus status, Pageable pageable);

    /**
     * Lấy danh sách verification của technician
     */
    Page<Verification> findByTechnicianId(Long technicianId, Pageable pageable);

    /**
     * Đếm verification pending
     */
    long countByStatus(KycStatus status);
}
