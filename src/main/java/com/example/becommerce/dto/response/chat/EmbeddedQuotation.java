package com.example.becommerce.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Compact quotation block embedded inside a quotation-typed message.
 * Mirrors the shape returned alongside messages in the API spec.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmbeddedQuotation {

    private String id;
    private String serviceName;
    private String description;
    private Long price;
    private LocalDateTime scheduledAt;
    private String status;
}
