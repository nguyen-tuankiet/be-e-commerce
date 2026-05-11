package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.TechnicianTier;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Technician-specific profile data attached to a User where role=TECHNICIAN.
 * One profile per user; created lazily on first access/update.
 */
@Entity
@Table(name = "technician_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_tech_profiles_user", columnNames = "user_id"),
        indexes = {
                @Index(name = "idx_tech_service_category", columnList = "service_category"),
                @Index(name = "idx_tech_is_available",     columnList = "is_available")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "service_category", length = 120)
    private String serviceCategory;

    @Column(name = "price_per_hour")
    private Long pricePerHour;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private boolean isAvailable = true;

    @Column(name = "time_available", length = 100)
    private String timeAvailable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TechnicianTier type = TechnicianTier.NORMAL;

    @Column(name = "title_badge", length = 100)
    private String titleBadge;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.NONE;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---- Skills (free-text labels) ---------------------------------

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "technician_skills",
            joinColumns = @JoinColumn(name = "profile_id"),
            indexes = @Index(name = "idx_tech_skills_profile", columnList = "profile_id"))
    @Column(name = "skill", length = 100)
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    // ---- Service areas (district names) ----------------------------

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "technician_service_areas",
            joinColumns = @JoinColumn(name = "profile_id"),
            indexes = @Index(name = "idx_tech_areas_profile", columnList = "profile_id"))
    @Column(name = "area", length = 100)
    @Builder.Default
    private List<String> areas = new ArrayList<>();

    // ---- Weekly schedule: day name -> "HH:mm-HH:mm" ----------------

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "technician_schedules",
            joinColumns = @JoinColumn(name = "profile_id"),
            indexes = @Index(name = "idx_tech_schedule_profile", columnList = "profile_id"))
    @MapKeyColumn(name = "day_of_week", length = 16)
    @Column(name = "time_range", length = 30)
    @Builder.Default
    private Map<String, String> schedule = new HashMap<>();
}
