package com.example.becommerce.repository;

import com.example.becommerce.entity.WarrantyClaim;
import com.example.becommerce.entity.enums.WarrantyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {

    /**
     * Tìm warranty claim theo mã
     */
    Optional<WarrantyClaim> findByCode(String code);

    /**
     * Lấy warranty claim theo order ID
     */
    Optional<WarrantyClaim> findByOrderId(Long orderId);

    /**
     * Lấy danh sách warranty claim của khách hàng
     */
    Page<WarrantyClaim> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Lấy danh sách warranty claim của thợ
     */
    Page<WarrantyClaim> findByTechnicianId(Long technicianId, Pageable pageable);

    /**
     * Lấy danh sách warranty claim theo status
     */
    Page<WarrantyClaim> findByStatus(WarrantyStatus status, Pageable pageable);

    /**
     * Đếm warranty claim pending
     */
    long countByStatus(WarrantyStatus status);
}
