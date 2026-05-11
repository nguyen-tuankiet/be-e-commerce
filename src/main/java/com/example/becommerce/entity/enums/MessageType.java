package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Discriminator for chat messages.
 * <ul>
 *   <li>{@link #TEXT} — plain text content</li>
 *   <li>{@link #IMAGE} — image URL in content</li>
 *   <li>{@link #QUOTATION} — embeds a {@link com.example.becommerce.entity.Quotation}</li>
 *   <li>{@link #SYSTEM} — bot/system-generated message (no sender)</li>
 * </ul>
 */
public enum MessageType {
    TEXT,
    IMAGE,
    QUOTATION,
    SYSTEM;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static MessageType from(String value) {
        if (value == null || value.isBlank()) return TEXT;
        return MessageType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
