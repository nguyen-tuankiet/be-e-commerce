package com.example.becommerce.repository;

import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    long countByDeletedFalse();

    long countByTechnician_IdAndStatusAndDeletedFalse(Long technicianId, OrderStatus status);
}
