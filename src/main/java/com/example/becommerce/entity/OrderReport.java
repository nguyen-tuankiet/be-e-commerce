package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.ReportReason;
import com.example.becommerce.entity.enums.ReportStatus;
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
 * A customer-filed incident report about a technician or an order.
 * Reviewed by admin via the admin reports endpoint.
 */
@Entity
@Table(name = "order_reports",
        indexes = {
                @Index(name = "idx_reports_code",   columnList = "code", unique = true),
                @Index(name = "idx_reports_status", columnList = "status"),
                @Index(name = "idx_reports_order",  columnList = "order_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable code, e.g. RPT-001. */
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    @ToString.Exclude
    private User technician;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.OPEN;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "order_report_evidence",
            joinColumns = @JoinColumn(name = "report_id"),
            indexes = @Index(name = "idx_report_evidence_report", columnList = "report_id"))
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> evidenceImages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
