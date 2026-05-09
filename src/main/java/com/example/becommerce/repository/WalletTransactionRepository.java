package com.example.becommerce.repository;

import com.example.becommerce.entity.WalletTransaction;
import com.example.becommerce.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Optional<WalletTransaction> findByTransactionCode(String transactionCode);

    Optional<WalletTransaction> findByTransactionCodeAndWallet_User_Id(String transactionCode, Long userId);

    Optional<WalletTransaction> findByGatewayTransactionId(String gatewayTransactionId);

    Page<WalletTransaction> findByWallet_User_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<WalletTransaction> findByWallet_User_IdAndTypeOrderByCreatedAtDesc(Long userId, TransactionType type, Pageable pageable);

    boolean existsByTransactionCode(String transactionCode);
}


