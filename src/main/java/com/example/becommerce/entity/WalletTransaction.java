package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.PaymentMethod;
import com.example.becommerce.entity.enums.TransactionStatus;
import com.example.becommerce.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable transaction ledger entry for wallet operations.
 */
@Entity
@Table(name = "wallet_transactions",
        indexes = {
                @Index(name = "idx_wallet_transactions_code", columnList = "transaction_code", unique = true),
                @Index(name = "idx_wallet_transactions_wallet_id", columnList = "wallet_id"),
                @Index(name = "idx_wallet_transactions_type", columnList = "type"),
                @Index(name = "idx_wallet_transactions_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", nullable = false, unique = true, length = 40)
    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false, length = 120)
    private String category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, precision = 19, scale = 0)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 0)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 19, scale = 0)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    @Column(name = "transfer_content", length = 255)
    private String transferContent;

    @Column(name = "gateway_request_id", length = 120)
    private String gatewayRequestId;

    @Column(name = "gateway_transaction_id", length = 120)
    private String gatewayTransactionId;

    @Column(name = "gateway_payload", columnDefinition = "TEXT")
    private String gatewayPayload;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}


