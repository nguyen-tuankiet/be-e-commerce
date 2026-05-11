package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.OrderActor;
import com.example.becommerce.entity.enums.OrderStatus;
import com.example.becommerce.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service repair order placed by a customer and fulfilled by a technician.
 * Mirrors the {@code orders} table from the GlowUp Concierge schema.
 */
@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_orders_code", columnList = "code", unique = true),
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_customer", columnList = "customer_id"),
                @Index(name = "idx_orders_technician", columnList = "technician_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable code, e.g. GU-99210 */
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    // ---- Service information ----------------------------------------

    @Column(name = "service_name", length = 200)
    private String serviceName;

    @Column(name = "sub_service", length = 200)
    private String subService;

    @Column(name = "service_category", length = 120)
    private String serviceCategory;

    @Column(name = "device_name", length = 200)
    private String deviceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ---- Logistics --------------------------------------------------

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "expected_time")
    private LocalDateTime expectedTime;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ---- Pricing ----------------------------------------------------

    @Column(name = "estimated_price")
    private Long estimatedPrice;

    @Column(name = "final_price")
    private Long finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    // ---- State ------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by", length = 20)
    private OrderActor cancelledBy;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ---- Audit ------------------------------------------------------

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    private boolean deleted = false;

    // ---- Relationships ---------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    @ToString.Exclude
    private User technician;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<OrderImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<OrderPriceAdjustment> priceAdjustments = new ArrayList<>();
}
