package com.example.becommerce.repository;

import com.example.becommerce.entity.OrderReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderReportRepository
        extends JpaRepository<OrderReport, Long>, JpaSpecificationExecutor<OrderReport> {

    boolean existsByCode(String code);

    long count();
}
