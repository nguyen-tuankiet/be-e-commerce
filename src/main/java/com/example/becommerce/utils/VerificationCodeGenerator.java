package com.example.becommerce.utils;

import com.example.becommerce.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generates verification codes (e.g. VR-2401) — base 2400 matches the
 * sample data in the API spec.
 */
@Component
@RequiredArgsConstructor
public class VerificationCodeGenerator {

    private static final String PREFIX = "VR-";
    private static final long   BASE   = 2400L;

    private final VerificationRepository verificationRepository;

    public String generate() {
        long next = verificationRepository.count() + BASE + 1;
        String code;
        do {
            code = PREFIX + next++;
        } while (verificationRepository.existsByCode(code));
        return code;
    }
}
