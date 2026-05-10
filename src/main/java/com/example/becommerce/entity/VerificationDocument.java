package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.DocType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Tài liệu xác minh (ảnh CMND, chứng chỉ, etc)
 */
@Entity
@Table(name = "verification_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Verification verification;

    /**
     * Loại tài liệu (ID_FRONT, ID_BACK, PORTRAIT, CERTIFICATE, SELFIE, OTHER)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocType docType;

    /**
     * URL của tài liệu
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
