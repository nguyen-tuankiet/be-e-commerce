package com.example.becommerce.repository;

import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    long countByDeletedFalse();

    long countByTechnician_IdAndStatusAndDeletedFalse(Long technicianId, OrderStatus status);
    long countByCompletedAtBetweenAndStatusAndDeletedFalse(LocalDateTime from, LocalDateTime to, OrderStatus status);

    // New: count completed orders in range (deleted=false)
    long countByCompletedAtBetweenAndDeletedFalse(LocalDateTime from, LocalDateTime to);

    // New: count by category within range (deleted=false)
    long countByCategoryIdAndCompletedAtBetweenAndDeletedFalse(Long categoryId, LocalDateTime from, LocalDateTime to);

    // Aggregate final_price grouped by month (yyyy-MM)
    @org.springframework.data.jpa.repository.Query(value = "SELECT to_char(completed_at, 'YYYY-MM') as period, COALESCE(SUM(final_price),0) as total " +
            "FROM orders " +
            "WHERE deleted = false AND completed_at BETWEEN :from AND :to " +
            "GROUP BY period ORDER BY period", nativeQuery = true)
    java.util.List<Object[]> sumFinalPriceGroupByMonth(java.time.LocalDateTime from, java.time.LocalDateTime to);

    // Aggregate final_price grouped by day (yyyy-MM-dd)
    @org.springframework.data.jpa.repository.Query(value = "SELECT to_char(completed_at, 'YYYY-MM-DD') as period, COALESCE(SUM(final_price),0) as total " +
            "FROM orders " +
            "WHERE deleted = false AND completed_at BETWEEN :from AND :to " +
            "GROUP BY period ORDER BY period", nativeQuery = true)
    java.util.List<Object[]> sumFinalPriceGroupByDay(java.time.LocalDateTime from, java.time.LocalDateTime to);

    // Recent orders page within completedAt range, ordered by scheduledAt desc
    Page<Order> findByCompletedAtBetweenAndDeletedFalseOrderByScheduledAtDesc(LocalDateTime from, LocalDateTime to, Pageable pageable);
}