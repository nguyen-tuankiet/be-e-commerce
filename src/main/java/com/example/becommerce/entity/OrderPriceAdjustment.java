package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.PriceAdjustmentStatus;
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
 * A technician's request to raise the final price beyond the original estimate.
 * Requires customer approval before the new price takes effect.
 */
@Entity
@Table(name = "order_price_adjustments",
        indexes = @Index(name = "idx_price_adj_order", columnList = "order_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPriceAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @Column(name = "original_price", nullable = false)
    private Long originalPrice;

    @Column(name = "new_price", nullable = false)
    private Long newPrice;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PriceAdjustmentStatus status = PriceAdjustmentStatus.PENDING;

    @Column(name = "requested_at")
    @CreationTimestamp
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Evidence images stored as URLs, one per row in adjustment_evidence (kept simple here). */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "order_price_adjustment_evidence",
            joinColumns = @JoinColumn(name = "adjustment_id"),
            indexes = @Index(name = "idx_price_adj_evidence", columnList = "adjustment_id"))
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> evidenceImages = new ArrayList<>();

    @OneToMany(mappedBy = "adjustment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<OrderPriceAdjustmentPart> parts = new ArrayList<>();
}
