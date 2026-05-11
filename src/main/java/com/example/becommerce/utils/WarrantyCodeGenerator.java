package com.example.becommerce.utils;

import com.example.becommerce.repository.WarrantyClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generates warranty claim codes (e.g. WR-99200) — base 99200 matches the
 * sample data in the API spec, then incremental from there.
 */
@Component
@RequiredArgsConstructor
public class WarrantyCodeGenerator {

    private static final String PREFIX = "WR-";
    private static final long   BASE   = 99200L;

    private final WarrantyClaimRepository warrantyRepository;

    public String generate() {
        long next = warrantyRepository.count() + BASE;
        String code;
        do {
            code = PREFIX + next++;
        } while (warrantyRepository.existsByCode(code));
        return code;
    }
}
