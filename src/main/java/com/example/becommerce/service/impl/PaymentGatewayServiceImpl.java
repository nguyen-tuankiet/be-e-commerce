package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.response.VnpayIpnResponse;
import com.example.becommerce.entity.Wallet;
import com.example.becommerce.entity.WalletTransaction;
import com.example.becommerce.entity.enums.PaymentMethod;
import com.example.becommerce.entity.enums.TransactionStatus;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.WalletRepository;
import com.example.becommerce.repository.WalletTransactionRepository;
import com.example.becommerce.service.PaymentGatewayService;
import com.example.becommerce.utils.HmacUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private static final DateTimeFormatter VNPAY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.payment.vnpay.tmn-code:}")
    private String vnpayTmnCode;
    @Value("${app.payment.vnpay.secret-key:}")
    private String vnpaySecretKey;
    @Value("${app.payment.vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpayPayUrl;
    @Value("${app.payment.vnpay.return-url:http://localhost:3000/wallet/topup-result}")
    private String vnpayReturnUrl;
    @Value("${app.payment.vnpay.ip-address:127.0.0.1}")
    private String vnpayIpAddress;

    @Override
    @Transactional
    public GatewayCheckoutData createCheckout(WalletTransaction transaction, PaymentMethod paymentMethod) {
        if (paymentMethod != PaymentMethod.VNPAY) {
            throw AppException.badRequest(ErrorCode.INVALID_PAYMENT_METHOD, "Hiện tại chỉ hỗ trợ thanh toán bằng VNPay");
        }
        return createVnpayCheckout(transaction);
    }

    @Override
    @Transactional
    public VnpayIpnResponse processVnpayIpn(Map<String, String> queryParams) {
        String secureHash = queryParams.get("vnp_SecureHash");
        if (secureHash == null || !isVnpaySignatureValid(queryParams, secureHash)) {
            return VnpayIpnResponse.builder().RspCode("97").Message("Invalid signature").build();
        }

        String transactionCode = queryParams.get("vnp_TxnRef");
        WalletTransaction tx = walletTransactionRepository.findByTransactionCode(transactionCode).orElse(null);
        if (tx == null) {
            return VnpayIpnResponse.builder().RspCode("01").Message("Transaction not found").build();
        }
        if (tx.getProcessedAt() != null || tx.getStatus() == TransactionStatus.SUCCESS) {
            return VnpayIpnResponse.builder().RspCode("02").Message("Already processed").build();
        }

        BigDecimal amount = new BigDecimal(queryParams.getOrDefault("vnp_Amount", "0")).divide(BigDecimal.valueOf(100));
        if (amount.compareTo(tx.getAmount()) != 0) {
            return VnpayIpnResponse.builder().RspCode("04").Message("Invalid amount").build();
        }

        boolean success = "00".equals(queryParams.get("vnp_ResponseCode"))
                && "00".equals(queryParams.get("vnp_TransactionStatus"));
        tx.setGatewayTransactionId(queryParams.get("vnp_TransactionNo"));
        tx.setGatewayPayload(toJsonSafe(queryParams));
        if (success) {
            confirmTopupAndCreditWallet(tx);
        } else {
            markTransactionFailed(tx);
        }
        return VnpayIpnResponse.builder().RspCode("00").Message("Confirm Success").build();
    }

    private GatewayCheckoutData createVnpayCheckout(WalletTransaction tx) {
        require(vnpayTmnCode, "VNPay TMN code is missing");
        require(vnpaySecretKey, "VNPay secret key is missing");

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime expireAt = tx.getExpiredAt() == null ? now.plusMinutes(30) : tx.getExpiredAt();
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayTmnCode);
        params.put("vnp_Amount", tx.getAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CreateDate", VNPAY_TIME_FORMAT.format(now));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", vnpayIpAddress);
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "Nap tien vi - " + tx.getTransactionCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", vnpayReturnUrl);
        params.put("vnp_TxnRef", tx.getTransactionCode());
        params.put("vnp_ExpireDate", VNPAY_TIME_FORMAT.format(expireAt));

        String secureHash = HmacUtils.hmacSha512(buildVnpayQuery(params), vnpaySecretKey);
        params.put("vnp_SecureHash", secureHash);

        tx.setGatewayRequestId(tx.getTransactionCode());
        tx.setGatewayPayload(toJsonSafe(params));
        walletTransactionRepository.save(tx);

        return new GatewayCheckoutData(vnpayPayUrl + "?" + buildVnpayQuery(params), null, null);
    }

    private boolean isVnpaySignatureValid(Map<String, String> params, String secureHash) {
        Map<String, String> filtered = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if ("vnp_SecureHash".equals(entry.getKey()) || "vnp_SecureHashType".equals(entry.getKey())) {
                continue;
            }
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return HmacUtils.hmacSha512(buildVnpayQuery(filtered), vnpaySecretKey).equalsIgnoreCase(secureHash);
    }

    private void confirmTopupAndCreditWallet(WalletTransaction tx) {
        Wallet wallet = walletRepository.findWithLockByUser_Id(tx.getWallet().getUser().getId())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy ví"));
        wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
        wallet.setTotalEarned(wallet.getTotalEarned().add(tx.getAmount()));
        walletRepository.save(wallet);

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setProcessedAt(LocalDateTime.now());
        walletTransactionRepository.save(tx);
    }

    private void markTransactionFailed(WalletTransaction tx) {
        tx.setStatus(TransactionStatus.FAILED);
        tx.setProcessedAt(LocalDateTime.now());
        walletTransactionRepository.save(tx);
    }

    private String buildVnpayQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    private String toJsonSafe(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw AppException.badRequest(ErrorCode.PAYMENT_GATEWAY_ERROR, message);
        }
    }
}




