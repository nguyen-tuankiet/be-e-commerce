package com.example.becommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A customer's review left on a completed order for the assigned technician.
 * One review per order — enforced via a unique constraint on order_id.
 */
@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_reviews_order", columnNames = "order_id"),
        indexes = {
                @Index(name = "idx_reviews_code",       columnList = "code", unique = true),
                @Index(name = "idx_reviews_technician", columnList = "technician_id"),
                @Index(name = "idx_reviews_author",     columnList = "author_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable code, e.g. REV-001. */
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    /** Customer who wrote the review. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private User author;

    /** Technician being reviewed — denormalized for fast lookups. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    @ToString.Exclude
    private User technician;

    /** 1 to 5 inclusive. Enforced in the service layer. */
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "review_images",
            joinColumns = @JoinColumn(name = "review_id"),
            indexes = @Index(name = "idx_review_images_review", columnList = "review_id"))
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> attachedImages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
