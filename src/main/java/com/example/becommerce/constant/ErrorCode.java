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

    // Order
    public static final String ORDER_NOT_FOUND                   = "ORDER_NOT_FOUND";
    public static final String ORDER_ALREADY_TAKEN               = "ORDER_ALREADY_TAKEN";
    public static final String INVALID_ORDER_STATUS_TRANSITION   = "INVALID_ORDER_STATUS_TRANSITION";
    public static final String PRICE_ADJUSTMENT_NOT_FOUND        = "PRICE_ADJUSTMENT_NOT_FOUND";

    // Review / Warranty / Report
    public static final String REVIEW_ALREADY_EXISTS             = "REVIEW_ALREADY_EXISTS";
    public static final String REVIEW_NOT_FOUND                  = "REVIEW_NOT_FOUND";
    public static final String WARRANTY_EXPIRED                  = "WARRANTY_EXPIRED";
    public static final String WARRANTY_NOT_FOUND                = "WARRANTY_NOT_FOUND";
    public static final String REPORT_NOT_FOUND                  = "REPORT_NOT_FOUND";

    // Technician / Verification
    public static final String TECHNICIAN_NOT_FOUND              = "TECHNICIAN_NOT_FOUND";
    public static final String VERIFICATION_NOT_FOUND            = "VERIFICATION_NOT_FOUND";
    public static final String VERIFICATION_PENDING_EXISTS       = "VERIFICATION_PENDING_EXISTS";

    // Chat / Quotation
    public static final String CONVERSATION_NOT_FOUND            = "CONVERSATION_NOT_FOUND";
    public static final String MESSAGE_NOT_FOUND                 = "MESSAGE_NOT_FOUND";
    public static final String QUOTATION_NOT_FOUND               = "QUOTATION_NOT_FOUND";
    public static final String QUOTATION_NOT_PENDING             = "QUOTATION_NOT_PENDING";
}
