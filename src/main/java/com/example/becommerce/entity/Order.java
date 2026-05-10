package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.OrderStatus;
import com.example.becommerce.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orders represent service requests from customers.
 * Maps to database 'orders' table.
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_orders_technician_id", columnList = "technician_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_status_created_at", columnList = "status, created_at"),
        @Index(name = "idx_orders_technician_status", columnList = "technician_id, status"),
        @Index(name = "idx_orders_customer_status", columnList = "customer_id, status"),
        @Index(name = "idx_orders_category_id", columnList = "category_id"),
        @Index(name = "idx_orders_scheduled_at", columnList = "scheduled_at"),
        @Index(name = "idx_orders_status_scheduled_at", columnList = "status, scheduled_at"),
        @Index(name = "idx_orders_district_city_status", columnList = "district, city, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false, length = 32)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Object category; // TODO: Link to ServiceCategory entity

    @Column(name = "service_name", length = 200)
    private String serviceName;

    @Column(name = "sub_service", length = 200)
    private String subService;

    @Column(name = "device_name", length = 200)
    private String deviceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Object addressId; // TODO: Link to UserAddress entity

    @Column(name = "address_text", columnDefinition = "TEXT", nullable = false)
    private String addressText;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(precision = 9, scale = 6)
    private Double latitude;

    @Column(precision = 9, scale = 6)
    private Double longitude;

    @Column(name = "estimated_price", nullable = false)
    private BigDecimal estimatedPrice = BigDecimal.ZERO;

    @Column(name = "final_price")
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "warranty_months", nullable = false)
    private Integer warrantyMonths = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.NEW;

    @Column(name = "expected_at")
    private LocalDateTime expectedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by", length = 30)
    private String cancelledBy;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
