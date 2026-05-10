package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Technician KYC verification.
 * Maps to 'verifications' table.
 */
@Entity
@Table(name = "verifications", indexes = {
        @Index(name = "idx_verifications_technician_id", columnList = "technician_id"),
        @Index(name = "idx_verifications_status", columnList = "status"),
        @Index(name = "idx_verifications_status_submitted", columnList = "status, submitted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verification {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false, length = 32)
    private String code;

    @Column(name = "technician_id", nullable = false)
    private UUID technicianId;

    @Column(name = "full_name_snapshot", nullable = false, length = 150)
    private String fullNameSnapshot;

    @Column(name = "phone_snapshot", length = 20)
    private String phoneSnapshot;

    @Column(name = "email_snapshot", length = 255)
    private String emailSnapshot;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(name = "service_category_id")
    private UUID serviceCategoryId;

    @Column(name = "service_category_text", length = 150)
    private String serviceCategoryText;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private KycStatus status = KycStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
    private User reviewedBy;

    /**
     * Thời gian review
     */
    private LocalDateTime reviewedAt;

    /**
     * Thời gian nộp
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Tài liệu đính kèm
     */
    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VerificationDocument> documents;
}
