package com.example.becommerce.utils;

import com.example.becommerce.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generates human-readable review codes (e.g. REV-001).
 * The total review count is used as the seed; we retry on collision.
 */
@Component
@RequiredArgsConstructor
public class ReviewCodeGenerator {

    private static final String PREFIX = "REV-";

    private final ReviewRepository reviewRepository;

    public String generate() {
        long next = reviewRepository.count() + 1;
        String code;
        do {
            code = PREFIX + String.format("%03d", next++);
        } while (reviewRepository.existsByCode(code));
        return code;
    }
}
