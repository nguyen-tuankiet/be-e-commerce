package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Returned after a successful refresh-token call.
 */
@Getter
@Builder
public class TokenRefreshResponse {

    private String accessToken;
}
