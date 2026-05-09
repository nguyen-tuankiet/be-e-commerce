package com.example.becommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Linked bank account for withdrawal requests.
 */
@Entity
@Table(name = "bank_accounts",
        indexes = {
                @Index(name = "idx_bank_accounts_code", columnList = "code", unique = true),
                @Index(name = "idx_bank_accounts_user_id", columnList = "user_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bank_name", nullable = false, length = 120)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 32)
    private String accountNumber;

    @Column(name = "account_owner", nullable = false, length = 120)
    private String accountOwner;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean defaultAccount = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

