package com.example.becommerce.utils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Mock QR generator used until a real VietQR integration is available.
 * Produces a base64 data URI containing a simple SVG placeholder.
 */
public final class VietQrGeneratorMock {

    private VietQrGeneratorMock() {}

    public static String generate(String bankName,
                                  String accountName,
                                  String accountNumber,
                                  String transferContent,
                                  BigDecimal amount) {

        String svg = """
                <svg xmlns='http://www.w3.org/2000/svg' width='320' height='320' viewBox='0 0 320 320'>
                  <rect width='320' height='320' fill='white'/>
                  <rect x='12' y='12' width='296' height='296' rx='18' fill='none' stroke='black' stroke-width='6'/>
                  <text x='28' y='54' font-family='Arial' font-size='20' font-weight='bold'>VietQR Mock</text>
                  <text x='28' y='92' font-family='Arial' font-size='14'>Bank: %s</text>
                  <text x='28' y='118' font-family='Arial' font-size='14'>Account: %s</text>
                  <text x='28' y='144' font-family='Arial' font-size='14'>Number: %s</text>
                  <text x='28' y='170' font-family='Arial' font-size='14'>Amount: %s</text>
                  <text x='28' y='196' font-family='Arial' font-size='14'>Content:</text>
                  <text x='28' y='220' font-family='Arial' font-size='13'>%s</text>
                  <rect x='28' y='244' width='264' height='44' fill='black'/>
                  <rect x='40' y='256' width='36' height='20' fill='white'/>
                  <rect x='92' y='256' width='24' height='20' fill='white'/>
                  <rect x='132' y='256' width='48' height='20' fill='white'/>
                  <rect x='196' y='256' width='30' height='20' fill='white'/>
                </svg>
                """.formatted(
                        escapeXml(bankName),
                        escapeXml(accountName),
                        escapeXml(accountNumber),
                        escapeXml(amount.toPlainString()),
                        escapeXml(transferContent)
                );

        String base64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }

    private static String escapeXml(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&apos;")
                .replace("\"", "&quot;");
    }
}

