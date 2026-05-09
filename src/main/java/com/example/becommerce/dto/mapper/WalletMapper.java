package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.WalletResponse;
import com.example.becommerce.entity.Wallet;
import org.springframework.stereotype.Component;

/**
 * Maps Wallet entities to API responses.
 */
@Component
public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        return WalletResponse.builder()
                .userId(wallet.getUser() != null ? wallet.getUser().getCode() : null)
                .balance(wallet.getBalance())
                .pendingBalance(wallet.getPendingBalance())
                .totalEarned(wallet.getTotalEarned())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .currency(wallet.getCurrency())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
