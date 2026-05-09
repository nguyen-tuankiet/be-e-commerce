package com.example.becommerce.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * HMAC helpers for payment gateway signatures.
 */
public final class HmacUtils {

    private HmacUtils() {
    }

    public static String hmacSha512(String data, String secret) {
        return hmac("HmacSHA512", data, secret);
    }

    public static String hmacSha256(String data, String secret) {
        return hmac("HmacSHA256", data, secret);
    }

    private static String hmac(String algorithm, String data, String secret) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot compute HMAC", ex);
        }
    }
}

