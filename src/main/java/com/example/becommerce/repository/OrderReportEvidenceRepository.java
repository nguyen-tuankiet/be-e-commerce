package com.example.becommerce.repository;

import com.example.becommerce.entity.OrderReportEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderReportEvidenceRepository extends JpaRepository<OrderReportEvidence, Long> {

    /**
     * Lấy danh sách ảnh chứng cứ của report
     */
    List<OrderReportEvidence> findByReportId(Long reportId);
}
