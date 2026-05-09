package com.example.becommerce.constant;

/**
 * Centralized API route constants.
 */
public final class ApiConstant {

    private ApiConstant() {}

    public static final String BASE_API      = "/api";
    public static final String AUTH_BASE     = BASE_API + "/auth";
    public static final String USER_BASE     = BASE_API + "/users";
    public static final String WALLET_BASE   = BASE_API + "/wallet";
    public static final String NOTIFICATION_BASE = BASE_API + "/notifications";
    public static final String ORDER_BASE     = BASE_API + "/orders";
    public static final String CONVERSATION_BASE = BASE_API + "/conversations";
    public static final String QUOTE_BASE        = BASE_API + "/quotes";

    // Auth endpoints
    public static final String AUTH_REGISTER         = "/register";
    public static final String AUTH_LOGIN            = "/login";
    public static final String AUTH_REFRESH          = "/refresh-token";
    public static final String AUTH_LOGOUT           = "/logout";
    public static final String AUTH_FORGOT_PASSWORD  = "/forgot-password";
    public static final String AUTH_CHANGE_PASSWORD  = "/change-password";
    public static final String AUTH_VERIFY_EMAIL     = "/verify-email";
    public static final String AUTH_ME               = "/me";

    // User endpoints
    public static final String USER_UPDATE_STATUS    = "/{id}/status";

    // Wallet endpoints
    public static final String WALLET_TRANSACTIONS   = "/transactions";
    public static final String WALLET_TOPUP          = "/topup";
    public static final String WALLET_TOPUP_CONFIRM  = "/topup/confirm";
    public static final String WALLET_WITHDRAW       = "/withdraw";
    public static final String WALLET_BANK_ACCOUNTS  = "/bank-accounts";

    // Notification endpoints
    public static final String NOTIFICATION_MARK_READ = "/{id}/read";
    public static final String NOTIFICATION_MARK_ALL_READ = "/read-all";
}
