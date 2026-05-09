package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.WalletTransactionResponse;
import com.example.becommerce.entity.WalletTransaction;
import com.example.becommerce.utils.MoneyUtils;
import org.springframework.stereotype.Component;

/**
 * Maps wallet transactions to history responses.
 */
@Component
public class WalletTransactionMapper {

    public WalletTransactionResponse toResponse(WalletTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        return WalletTransactionResponse.builder()
                .id(transaction.getTransactionCode())
                .type(transaction.getType().apiValue())
                .title(transaction.getTitle())
                .category(transaction.getCategory())
                .amount(MoneyUtils.displayAmount(transaction.getType(), transaction.getAmount()))
                .status(transaction.getStatus().apiValue())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
