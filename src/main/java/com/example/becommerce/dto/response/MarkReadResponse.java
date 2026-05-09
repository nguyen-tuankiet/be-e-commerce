package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response for marking a single notification as read.
 */
@Getter
@Builder
public class MarkReadResponse {

    private String id;
    private boolean isRead;
}

