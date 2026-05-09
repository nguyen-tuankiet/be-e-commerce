package com.example.becommerce.service;

import com.example.becommerce.dto.response.VnpayIpnResponse;
import com.example.becommerce.entity.WalletTransaction;
import com.example.becommerce.entity.enums.PaymentMethod;

import java.util.Map;

/**
 * Payment gateway orchestrator for VNPay.
 */
public interface PaymentGatewayService {

    GatewayCheckoutData createCheckout(WalletTransaction transaction, PaymentMethod paymentMethod);

    VnpayIpnResponse processVnpayIpn(Map<String, String> queryParams);

    record GatewayCheckoutData(String checkoutUrl, String deepLink, String qrCodeUrl) {}
}


