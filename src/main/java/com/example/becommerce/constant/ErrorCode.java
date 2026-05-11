package com.example.becommerce.constant;

/**
 * Application-wide error codes returned in API error responses.
 */
public final class ErrorCode {

    private ErrorCode() {}

    // Generic
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String VALIDATION_ERROR      = "VALIDATION_ERROR";
    public static final String NOT_FOUND             = "NOT_FOUND";
    public static final String BAD_REQUEST           = "BAD_REQUEST";
    public static final String FORBIDDEN             = "FORBIDDEN";
    public static final String UNAUTHORIZED          = "UNAUTHORIZED";

    // Auth
    public static final String EMAIL_ALREADY_EXISTS      = "EMAIL_ALREADY_EXISTS";
    public static final String PHONE_ALREADY_EXISTS      = "PHONE_ALREADY_EXISTS";
    public static final String INVALID_CREDENTIALS       = "INVALID_CREDENTIALS";
    public static final String INVALID_TOKEN             = "INVALID_TOKEN";
    public static final String TOKEN_EXPIRED             = "TOKEN_EXPIRED";
    public static final String TOKEN_REVOKED             = "TOKEN_REVOKED";
    public static final String ROLE_NOT_ALLOWED          = "ROLE_NOT_ALLOWED";
    public static final String ACCOUNT_LOCKED            = "ACCOUNT_LOCKED";
    public static final String ACCOUNT_DISABLED          = "ACCOUNT_DISABLED";
    public static final String PASSWORDS_DO_NOT_MATCH    = "PASSWORDS_DO_NOT_MATCH";
    public static final String EMAIL_NOT_VERIFIED        = "EMAIL_NOT_VERIFIED";
    public static final String EMAIL_VERIFICATION_FAILED = "EMAIL_VERIFICATION_FAILED";

    // User
    public static final String USER_NOT_FOUND          = "USER_NOT_FOUND";
    public static final String INVALID_STATUS          = "INVALID_STATUS";

    // Wallet
    public static final String WALLET_NOT_FOUND                 = "WALLET_NOT_FOUND";
    public static final String WALLET_ALREADY_EXISTS            = "WALLET_ALREADY_EXISTS";
    public static final String TRANSACTION_NOT_FOUND            = "TRANSACTION_NOT_FOUND";
    public static final String TRANSACTION_EXPIRED              = "TRANSACTION_EXPIRED";
    public static final String TRANSACTION_ALREADY_VERIFIED     = "TRANSACTION_ALREADY_VERIFIED";
    public static final String INSUFFICIENT_BALANCE             = "INSUFFICIENT_BALANCE";
    public static final String INVALID_TRANSACTION_TYPE         = "INVALID_TRANSACTION_TYPE";
    public static final String INVALID_PAYMENT_METHOD           = "INVALID_PAYMENT_METHOD";
    public static final String TOPUP_AMOUNT_TOO_SMALL           = "TOPUP_AMOUNT_TOO_SMALL";
    public static final String WITHDRAW_AMOUNT_TOO_SMALL        = "WITHDRAW_AMOUNT_TOO_SMALL";
    public static final String BANK_ACCOUNT_NOT_FOUND           = "BANK_ACCOUNT_NOT_FOUND";
    public static final String BANK_ACCOUNT_ALREADY_EXISTS      = "BANK_ACCOUNT_ALREADY_EXISTS";
    public static final String BANK_ACCOUNT_DELETE_NOT_ALLOWED  = "BANK_ACCOUNT_DELETE_NOT_ALLOWED";
    public static final String BANK_ACCOUNT_OWNER_MISMATCH      = "BANK_ACCOUNT_OWNER_MISMATCH";
    public static final String PAYMENT_GATEWAY_ERROR            = "PAYMENT_GATEWAY_ERROR";

    // Notification
    public static final String NOTIFICATION_NOT_FOUND           = "NOTIFICATION_NOT_FOUND";
    public static final String NOTIFICATION_CREATE_FAILED       = "NOTIFICATION_CREATE_FAILED";

    // Category
    public static final String CATEGORY_NOT_FOUND               = "CATEGORY_NOT_FOUND";
    public static final String INVALID_CATEGORY_PRIORITY        = "INVALID_CATEGORY_PRIORITY";

    // Admin finance
    public static final String INVALID_WITHDRAW_REQUEST         = "INVALID_WITHDRAW_REQUEST";

    // File upload
    public static final String INVALID_FILE                     = "INVALID_FILE";
    public static final String INVALID_FILE_TYPE                = "INVALID_FILE_TYPE";
    public static final String INVALID_FILE_FOLDER              = "INVALID_FILE_FOLDER";
    public static final String FILE_UPLOAD_FAILED               = "FILE_UPLOAD_FAILED";
}
