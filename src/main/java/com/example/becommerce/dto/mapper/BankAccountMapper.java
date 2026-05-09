package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.BankAccountResponse;
import com.example.becommerce.entity.BankAccount;
import com.example.becommerce.utils.BankAccountMaskUtils;
import org.springframework.stereotype.Component;

/**
 * Maps bank account entities to safe API responses.
 */
@Component
public class BankAccountMapper {

    public BankAccountResponse toResponse(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        return BankAccountResponse.builder()
                .id(bankAccount.getCode())
                .bankName(bankAccount.getBankName())
                .accountNumber(BankAccountMaskUtils.mask(bankAccount.getAccountNumber()))
                .accountOwner(bankAccount.getAccountOwner())
                .isDefault(bankAccount.isDefaultAccount())
                .createdAt(bankAccount.getCreatedAt())
                .build();
    }
}
