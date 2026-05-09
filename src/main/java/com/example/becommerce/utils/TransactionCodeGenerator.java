package com.example.becommerce.utils;

import com.example.becommerce.constant.WalletConstant;
import com.example.becommerce.entity.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Generates human-readable transaction and bank account codes.
 */
@Component
public class TransactionCodeGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    public String generateTransactionCode(TransactionType type) {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return WalletConstant.TRANSACTION_CODE_PREFIX + "-" + type.name() + "-" + DATE_FORMAT.format(LocalDateTime.now()) + "-" + suffix;
    }

    public String generateBankAccountCode() {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return WalletConstant.BANK_ACCOUNT_CODE_PREFIX + "-" + DATE_FORMAT.format(LocalDateTime.now()) + "-" + suffix;
    }
}

