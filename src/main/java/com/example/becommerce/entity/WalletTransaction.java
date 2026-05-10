package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.TransactionStatus;
import com.example.becommerce.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable transaction ledger entry for wallet operations.
 * Maps to 'wallet_transactions' table.
 */
@Entity
@Table(name = "wallet_transactions",
        indexes = {
                @Index(name = "idx_wallet_transactions_code", columnList = "code", unique = true),
                @Index(name = "idx_wallet_transactions_wallet_user_id", columnList = "wallet_user_id"),
                @Index(name = "idx_wallet_transactions_wallet_user_created", columnList = "wallet_user_id, created_at"),
                @Index(name = "idx_wallet_transactions_type", columnList = "type"),
                @Index(name = "idx_wallet_transactions_status", columnList = "status"),
                @Index(name = "idx_wallet_transactions_ref_type_id", columnList = "ref_type, ref_id"),
                @Index(name = "idx_wallet_transactions_wallet_type_status", columnList = "wallet_user_id, type, status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "wallet_user_id", nullable = false)
    private UUID walletUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_user_id", referencedColumnName = "userId", nullable = false, insertable = false, updatable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 0)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 0)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 19, scale = 0)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(name = "balance_after", precision = 19, scale = 0)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ref_type", length = 30)
    private String refType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;
}


