package com.example.becommerce.utils;

import com.example.becommerce.constant.NotificationConstant;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates notification code in format: NOTIF-yyyyMMdd-0001.
 */
@Component
public class NotificationCodeGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate(LocalDate date, long sequence) {
        return NotificationConstant.CODE_PREFIX + "-" + DATE_FORMAT.format(date) + "-" + String.format("%04d", sequence);
    }
}

