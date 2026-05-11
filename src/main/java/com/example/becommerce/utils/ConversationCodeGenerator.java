package com.example.becommerce.utils;

import com.example.becommerce.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Generates codes like CONV-001. */
@Component
@RequiredArgsConstructor
public class ConversationCodeGenerator {

    private static final String PREFIX = "CONV-";
    private final ConversationRepository conversationRepository;

    public String generate() {
        long next = conversationRepository.count() + 1;
        String code;
        do {
            code = PREFIX + String.format("%03d", next++);
        } while (conversationRepository.existsByCode(code));
        return code;
    }
}
