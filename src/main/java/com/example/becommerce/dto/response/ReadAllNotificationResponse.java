package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response for bulk mark-as-read operation.
 */
@Getter
@Builder
public class ReadAllNotificationResponse {

    private int updatedCount;
}

