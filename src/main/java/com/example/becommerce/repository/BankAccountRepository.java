package com.example.becommerce.repository;

import com.example.becommerce.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUser_IdOrderByCreatedAtAsc(Long userId);

    long countByUser_Id(Long userId);

    Optional<BankAccount> findByCodeAndUser_Id(String code, Long userId);

    boolean existsByCode(String code);

    boolean existsByUser_IdAndAccountNumber(Long userId, String accountNumber);
}


