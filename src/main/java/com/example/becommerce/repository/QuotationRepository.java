package com.example.becommerce.repository;

import com.example.becommerce.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByCode(String code);

    boolean existsByCode(String code);

    long count();
}
