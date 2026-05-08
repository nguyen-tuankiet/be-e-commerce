package com.example.becommerce.utils;

import com.example.becommerce.entity.User;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility for generating human-readable user codes like USR-001.
 */
@Component
public class UserCodeGenerator {

    private static final String PREFIX = "USR-";

    /**
     * Generates a zero-padded user code.
     * e.g., sequence=1 → "USR-001", sequence=100 → "USR-100"
     *
     * @param sequence the next sequence number (usually totalUsers + 1)
     */
    public String generate(long sequence) {
        return PREFIX + String.format("%03d", sequence);
    }
}
