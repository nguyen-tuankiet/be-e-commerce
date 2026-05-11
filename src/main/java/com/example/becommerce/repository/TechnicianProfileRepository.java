package com.example.becommerce.repository;

import com.example.becommerce.entity.TechnicianProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TechnicianProfileRepository
        extends JpaRepository<TechnicianProfile, Long>, JpaSpecificationExecutor<TechnicianProfile> {

    Optional<TechnicianProfile> findByUser_Id(Long userId);

    Optional<TechnicianProfile> findByUser_Code(String userCode);

    boolean existsByUser_Id(Long userId);
}
