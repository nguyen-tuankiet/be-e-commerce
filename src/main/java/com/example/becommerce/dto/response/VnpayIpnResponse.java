package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * VNPay IPN acknowledgement payload.
 */
@Getter
@Builder
public class VnpayIpnResponse {
    private String RspCode;
    private String Message;
}

