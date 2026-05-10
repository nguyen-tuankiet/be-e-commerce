package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.PriceAdjustmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order price adjustment (extra cost request).
 * Maps to 'order_price_adjustments' table.
 */
@Entity
@Table(name = "order_price_adjustments", indexes = {
        @Index(name = "idx_price_adj_order_id", columnList = "order_id"),
        @Index(name = "idx_price_adj_status", columnList = "status"),
        @Index(name = "idx_price_adj_order_status", columnList = "order_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPriceAdjustment {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @Column(name = "original_price", nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "new_price", nullable = false)
    private BigDecimal newPrice;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PriceAdjustmentStatus status = PriceAdjustmentStatus.PENDING;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
