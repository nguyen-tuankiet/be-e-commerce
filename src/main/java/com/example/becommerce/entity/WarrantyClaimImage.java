package com.example.becommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Images attached to warranty claims.
 * Maps to 'warranty_claim_images' table.
 */
@Entity
@Table(name = "warranty_claim_images", indexes = {
        @Index(name = "idx_warranty_images_warranty_id", columnList = "warranty_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyClaimImage {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private WarrantyClaim warranty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
