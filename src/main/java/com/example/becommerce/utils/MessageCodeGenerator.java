package com.example.becommerce.utils;

import com.example.becommerce.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Generates codes like MSG-001. */
@Component
@RequiredArgsConstructor
public class MessageCodeGenerator {

    private static final String PREFIX = "MSG-";
    private final MessageRepository messageRepository;

    public String generate() {
        long next = messageRepository.count() + 1;
        String code;
        do {
            code = PREFIX + String.format("%03d", next++);
        } while (messageRepository.existsByCode(code));
        return code;
    }
}
