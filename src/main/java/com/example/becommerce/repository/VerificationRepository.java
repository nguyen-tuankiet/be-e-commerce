package com.example.becommerce.repository;

import com.example.becommerce.entity.Verification;
import com.example.becommerce.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepository
        extends JpaRepository<Verification, Long>, JpaSpecificationExecutor<Verification> {

    Optional<Verification> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Verification> findTopByTechnician_IdOrderBySubmittedAtDesc(Long technicianId);

    boolean existsByTechnician_IdAndStatus(Long technicianId, VerificationStatus status);

    long count();
}
