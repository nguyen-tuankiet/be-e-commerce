package com.example.becommerce.repository;

import com.example.becommerce.entity.WarrantyClaimImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyClaimImageRepository extends JpaRepository<WarrantyClaimImage, Long> {

    /**
     * Lấy danh sách ảnh của warranty claim
     */
    List<WarrantyClaimImage> findByWarrantyClaimId(Long warrantyClaimId);
}
