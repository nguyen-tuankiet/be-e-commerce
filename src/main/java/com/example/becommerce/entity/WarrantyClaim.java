package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.WarrantyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Warranty claim from customer.
 * Maps to 'warranty_claims' table.
 */
@Entity
@Table(name = "warranty_claims", indexes = {
        @Index(name = "idx_warranty_claims_order_id", columnList = "order_id"),
        @Index(name = "idx_warranty_claims_customer_id", columnList = "customer_id"),
        @Index(name = "idx_warranty_claims_status_created", columnList = "status, created_at"),
        @Index(name = "idx_warranty_claims_warranty_expires_at", columnList = "warranty_expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyClaim {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false, length = 32)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private WarrantyStatus status = WarrantyStatus.PENDING;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "warranty_expires_at", nullable = false)
    private LocalDateTime warrantyExpiresAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
     */
    @OneToMany(mappedBy = "warrantyClaim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WarrantyClaimImage> images;
}
