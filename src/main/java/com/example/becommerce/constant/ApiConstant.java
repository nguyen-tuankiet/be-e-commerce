package com.example.becommerce.constant;

/**
 * Centralized API route constants.
 */
public final class ApiConstant {

    private ApiConstant() {}

    public static final String BASE_API      = "/api";
    public static final String AUTH_BASE     = BASE_API + "/auth";
    public static final String USER_BASE     = BASE_API + "/users";

    // Auth endpoints
    public static final String AUTH_REGISTER         = "/register";
    public static final String AUTH_LOGIN            = "/login";
    public static final String AUTH_REFRESH          = "/refresh-token";
    public static final String AUTH_LOGOUT           = "/logout";
    public static final String AUTH_FORGOT_PASSWORD  = "/forgot-password";
    public static final String AUTH_CHANGE_PASSWORD  = "/change-password";
    public static final String AUTH_ME               = "/me";

    // User endpoints
    public static final String USER_UPDATE_STATUS    = "/{id}/status";
}
