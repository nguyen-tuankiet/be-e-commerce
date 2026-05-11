package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.VerificationStatus;
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

/**
 * Technician KYC submission. One row per submission;
 * a technician can re-submit after a rejection so we keep history here.
 */
@Entity
@Table(name = "verifications",
        indexes = {
                @Index(name = "idx_verifications_code",       columnList = "code", unique = true),
                @Index(name = "idx_verifications_technician", columnList = "technician_id"),
                @Index(name = "idx_verifications_status",     columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable code, e.g. VR-2401. */
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    @ToString.Exclude
    private User technician;

    @Column(length = 120)
    private String district;

    @Column(name = "service_category", length = 120)
    private String serviceCategory;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    // ---- KYC documents (URLs stored on object storage / local FS) --

    @Column(name = "id_front", length = 500)
    private String idFront;

    @Column(name = "id_back", length = 500)
    private String idBack;

    @Column(length = 500)
    private String portrait;

    @Column(length = 500)
    private String certificate;

    // ---- Review state ----------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "reviewed_by", length = 50)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
