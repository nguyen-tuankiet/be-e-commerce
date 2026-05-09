package com.example.becommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * MoMo IPN callback payload.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MomoIpnRequest {

    private String partnerCode;
    private String orderId;
    private String requestId;
    private String amount;
    private String orderInfo;
    private String orderType;
    private String transId;
    private String resultCode;
    private String message;
    private String payType;
    private String responseTime;
    private String extraData;
    private String signature;
}
