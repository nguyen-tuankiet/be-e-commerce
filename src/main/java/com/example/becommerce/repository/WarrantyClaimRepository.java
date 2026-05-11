package com.example.becommerce.repository;

import com.example.becommerce.entity.WarrantyClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {

    Optional<WarrantyClaim> findByOrder_Id(Long orderId);

    Optional<WarrantyClaim> findTopByOrder_IdOrderByCreatedAtDesc(Long orderId);

    boolean existsByOrder_Id(Long orderId);

    boolean existsByCode(String code);

    long count();
}
