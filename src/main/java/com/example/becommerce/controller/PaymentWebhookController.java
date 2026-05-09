package com.example.becommerce.controller;

import com.example.becommerce.dto.response.VnpayIpnResponse;
import com.example.becommerce.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public webhook endpoints used by payment providers.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final WalletService walletService;

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<VnpayIpnResponse> vnpayIpn(@RequestParam Map<String, String> queryParams) {
        return ResponseEntity.ok(walletService.handleVnpayIpn(queryParams));
    }
}
