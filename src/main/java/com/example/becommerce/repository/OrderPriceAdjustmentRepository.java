package com.example.becommerce.repository;

import com.example.becommerce.entity.OrderPriceAdjustment;
import com.example.becommerce.entity.enums.PriceAdjustmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderPriceAdjustmentRepository extends JpaRepository<OrderPriceAdjustment, Long> {

    Optional<OrderPriceAdjustment> findTopByOrder_IdOrderByRequestedAtDesc(Long orderId);

    Optional<OrderPriceAdjustment> findTopByOrder_IdAndStatusOrderByRequestedAtDesc(
            Long orderId, PriceAdjustmentStatus status);
}
