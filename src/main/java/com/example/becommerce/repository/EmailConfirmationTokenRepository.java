package com.example.becommerce.repository;

import com.example.becommerce.entity.EmailConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, Long> {

    Optional<EmailConfirmationToken> findByTokenAndUsedFalse(String token);

    /** Invalidate all existing confirmation tokens for a user before issuing new one */
    @Modifying
    @Query("UPDATE EmailConfirmationToken e SET e.used = true WHERE e.user.id = :userId")
    void invalidateAllByUserId(@Param("userId") Long userId);
}

