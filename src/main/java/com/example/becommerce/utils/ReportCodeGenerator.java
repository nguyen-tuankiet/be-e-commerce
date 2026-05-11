package com.example.becommerce.utils;

import com.example.becommerce.repository.OrderReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generates order report codes (e.g. RPT-001).
 */
@Component
@RequiredArgsConstructor
public class ReportCodeGenerator {

    private static final String PREFIX = "RPT-";

    private final OrderReportRepository reportRepository;

    public String generate() {
        long next = reportRepository.count() + 1;
        String code;
        do {
            code = PREFIX + String.format("%03d", next++);
        } while (reportRepository.existsByCode(code));
        return code;
    }
}
