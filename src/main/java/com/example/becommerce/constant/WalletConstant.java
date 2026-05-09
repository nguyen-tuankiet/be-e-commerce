package com.example.becommerce.constant;

import java.math.BigDecimal;

/**
 * Wallet module defaults and prefixes.
 */
public final class WalletConstant {

    private WalletConstant() {}

    public static final String DEFAULT_CURRENCY = "VND";
    public static final BigDecimal DEFAULT_TOPUP_MIN_AMOUNT = new BigDecimal("10000");
    public static final BigDecimal DEFAULT_WITHDRAW_MIN_AMOUNT = new BigDecimal("50000");
    public static final BigDecimal DEFAULT_WITHDRAW_FEE = new BigDecimal("5000");
    public static final int DEFAULT_TOPUP_EXPIRE_MINUTES = 30;

    public static final String TRANSACTION_CODE_PREFIX = "TX";
    public static final String BANK_ACCOUNT_CODE_PREFIX = "BANK";
}
