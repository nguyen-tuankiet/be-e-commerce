package com.example.becommerce.repository;

import com.example.becommerce.entity.WalletTransaction;
import com.example.becommerce.entity.enums.TransactionStatus;
import com.example.becommerce.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long>, JpaSpecificationExecutor<WalletTransaction> {

    Optional<WalletTransaction> findByTransactionCode(String transactionCode);

    Optional<WalletTransaction> findByTransactionCodeAndWallet_User_Id(String transactionCode, Long userId);

    Optional<WalletTransaction> findByGatewayTransactionId(String gatewayTransactionId);

    Page<WalletTransaction> findByWallet_User_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<WalletTransaction> findByWallet_User_IdAndTypeOrderByCreatedAtDesc(Long userId, TransactionType type, Pageable pageable);

    List<WalletTransaction> findByTypeOrderByCreatedAtDesc(TransactionType type);

    long countByTypeAndStatus(TransactionType type, TransactionStatus status);

    @Query("select coalesce(sum(t.netAmount), 0) from WalletTransaction t where t.status = :status")
    BigDecimal sumNetAmountByStatus(@Param("status") TransactionStatus status);

    @Query("select coalesce(sum(t.fee), 0) from WalletTransaction t where t.status = :status")
    BigDecimal sumFeeByStatus(@Param("status") TransactionStatus status);

    @Query("select coalesce(sum(t.netAmount), 0) from WalletTransaction t where t.status = :status and t.createdAt >= :from and t.createdAt < :to")
    BigDecimal sumNetAmountByStatusAndCreatedAtBetween(
            @Param("status") TransactionStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    boolean existsByTransactionCode(String transactionCode);
}
