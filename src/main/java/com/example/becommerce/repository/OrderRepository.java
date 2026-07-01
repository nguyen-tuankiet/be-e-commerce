package com.example.becommerce.repository;

import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    @Query("SELECT o FROM Order o WHERE o.technician.id = :technicianId " +
            "AND o.status IN (com.example.becommerce.entity.enums.OrderStatus.SCHEDULED, " +
            "                 com.example.becommerce.entity.enums.OrderStatus.IN_PROGRESS, " +
            "                 com.example.becommerce.entity.enums.OrderStatus.AWAITING_PAYMENT) " +
            "AND (o.scheduledAt >= :fromDate OR o.expectedTime >= :fromDate) " +
            "AND o.deleted = false")
    List<Order> findBusySlotsByTechnician(
            @Param("technicianId") Long technicianId,
            @Param("fromDate") LocalDateTime fromDate);

    // Đếm tổng số đơn thợ đã nhận (bỏ qua đơn bị hủy)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.technician.id = :techId AND o.status <> 'CANCELLED'")
    long countActiveOrdersByTechnician(@Param("techId") Long techId);

    // Đếm số đơn đã hoàn thành của thợ
    @Query("SELECT COUNT(o) FROM Order o WHERE o.technician.id = :techId AND o.status = 'COMPLETED'")
    long countCompletedOrdersByTechnician(@Param("techId") Long techId);

    // Lấy danh sách các đơn đã hoàn thành trong một khoảng thời gian để tính doanh thu
    @Query("SELECT o FROM Order o WHERE o.technician.id = :techId AND o.status = 'COMPLETED' " +
            "AND o.completedAt >= :startDate AND o.completedAt <= :endDate")
    List<Order> findCompletedOrdersForChart(
            @Param("techId") Long techId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}