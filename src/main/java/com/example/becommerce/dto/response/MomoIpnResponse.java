package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * MoMo IPN acknowledgement payload.
 */
@Getter
@Builder
public class MomoIpnResponse {
    private int resultCode;
    private String message;
}

