package com.example.becommerce.utils;

import com.example.becommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generates human-readable order codes (e.g. GU-99210).
 * <p>
 * Starts at 99210 to match the sample data in the API spec and produces
 * incrementing codes thereafter, retrying if a collision is detected.
 */
@Component
@RequiredArgsConstructor
public class OrderCodeGenerator {

    private static final String PREFIX  = "GU-";
    private static final long   BASE    = 99210L;

    private final OrderRepository orderRepository;

    public String generate() {
        long next = orderRepository.countByDeletedFalse() + BASE;
        String code;
        do {
            code = PREFIX + next++;
        } while (orderRepository.existsByCode(code));
        return code;
    }
}
