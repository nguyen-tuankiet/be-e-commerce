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
    public static final String EMAIL_ALREADY_EXISTS    = "EMAIL_ALREADY_EXISTS";
    public static final String PHONE_ALREADY_EXISTS    = "PHONE_ALREADY_EXISTS";
    public static final String INVALID_CREDENTIALS     = "INVALID_CREDENTIALS";
    public static final String INVALID_TOKEN           = "INVALID_TOKEN";
    public static final String TOKEN_EXPIRED           = "TOKEN_EXPIRED";
    public static final String TOKEN_REVOKED           = "TOKEN_REVOKED";
    public static final String ROLE_NOT_ALLOWED        = "ROLE_NOT_ALLOWED";
    public static final String ACCOUNT_LOCKED          = "ACCOUNT_LOCKED";
    public static final String ACCOUNT_DISABLED        = "ACCOUNT_DISABLED";
    public static final String PASSWORDS_DO_NOT_MATCH  = "PASSWORDS_DO_NOT_MATCH";

    // User
    public static final String USER_NOT_FOUND          = "USER_NOT_FOUND";
    public static final String INVALID_STATUS          = "INVALID_STATUS";
}
