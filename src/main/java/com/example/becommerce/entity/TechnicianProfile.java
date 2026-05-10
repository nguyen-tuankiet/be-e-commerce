package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.VerificationStatus;
import com.example.becommerce.entity.enums.TechTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Technician profile entity.
 * Maps to 'technician_profiles' table where user_id is the primary key.
 */
@Entity
@Table(name = "technician_profiles", indexes = {
        @Index(name = "idx_tech_profiles_is_available", columnList = "is_available"),
        @Index(name = "idx_tech_profiles_rating_avg", columnList = "rating_avg"),
        @Index(name = "idx_tech_profiles_tier", columnList = "tier"),
        @Index(name = "idx_tech_profiles_primary_category_id", columnList = "primary_category_id"),
        @Index(name = "idx_tech_profiles_is_available_rating", columnList = "is_available, rating_avg"),
        @Index(name = "idx_tech_profiles_verification_tier", columnList = "verification_status, tier")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianProfile {

    @Id
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(name = "primary_category_id")
    private UUID primaryCategoryId;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "price_per_hour")
    private BigDecimal pricePerHour;

    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    @Builder.Default
    private Integer ratingCount = 0;

    @Column(name = "completed_jobs", nullable = false)
    @Builder.Default
    private Integer completedJobs = 0;

    @Column(name = "cancelled_jobs", nullable = false)
    @Builder.Default
    private Integer cancelledJobs = 0;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = false;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TechTier tier = TechTier.NORMAL;

    @Column(name = "title_badge", length = 100)
    private String titleBadge;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.NONE;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
