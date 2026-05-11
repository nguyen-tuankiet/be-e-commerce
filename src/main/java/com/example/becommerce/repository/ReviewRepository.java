package com.example.becommerce.repository;

import com.example.becommerce.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrder_Id(Long orderId);

    boolean existsByCode(String code);

    long count();

    Optional<Review> findByCode(String code);

    Page<Review> findByTechnician_IdOrderByCreatedAtDesc(Long technicianId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.technician.id = :technicianId")
    Double averageRatingByTechnician(@Param("technicianId") Long technicianId);

    long countByTechnician_Id(Long technicianId);
}
