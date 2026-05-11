package com.example.becommerce.repository;

import com.example.becommerce.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    boolean existsByUser_IdAndCurrency(Long userId, String currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findWithLockByUser_Id(Long userId);

    @Query("select coalesce(sum(w.balance), 0) from Wallet w")
    BigDecimal sumBalance();
}

