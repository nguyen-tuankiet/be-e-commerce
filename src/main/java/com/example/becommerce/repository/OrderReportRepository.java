package com.example.becommerce.repository;

import com.example.becommerce.entity.OrderReport;
import com.example.becommerce.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderReportRepository extends JpaRepository<OrderReport, Long> {

    /**
     * Tìm report theo mã
     */
    Optional<OrderReport> findByCode(String code);

    /**
     * Lấy report của order
     */
    Optional<OrderReport> findByOrderId(Long orderId);

    /**
     * Lấy danh sách report của người gửi
     */
    Page<OrderReport> findByReporterId(Long reporterId, Pageable pageable);

    /**
     * Lấy danh sách report theo status (admin)
     */
    Page<OrderReport> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * Đếm report open
     */
    long countByStatus(ReportStatus status);
}
