package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.QuotationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Quotation (price quote) sent by technician to customer.
 * Maps to 'quotations' table.
 */
@Entity
@Table(name = "quotations", indexes = {
        @Index(name = "idx_quotations_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_quotations_technician_id", columnList = "technician_id"),
        @Index(name = "idx_quotations_status", columnList = "status"),
        @Index(name = "idx_quotations_technician_status", columnList = "technician_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false, length = 32)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Conversation conversation;

    @Column(name = "technician_id")
    private UUID technicianId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "service_name", length = 200)
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 0)
    private BigDecimal price;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private QuotationStatus status = QuotationStatus.PENDING;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
