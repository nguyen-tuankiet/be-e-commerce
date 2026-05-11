package com.example.becommerce.utils;

import com.example.becommerce.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Generates codes like QUOTE-001. */
@Component
@RequiredArgsConstructor
public class QuotationCodeGenerator {

    private static final String PREFIX = "QUOTE-";
    private final QuotationRepository quotationRepository;

    public String generate() {
        long next = quotationRepository.count() + 1;
        String code;
        do {
            code = PREFIX + String.format("%03d", next++);
        } while (quotationRepository.existsByCode(code));
        return code;
    }
}
