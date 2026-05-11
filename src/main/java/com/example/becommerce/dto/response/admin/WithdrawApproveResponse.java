package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WithdrawApproveResponse {
    private final String id;
    private final String status;
    private final LocalDateTime processedAt;
    private final String processedBy;
}
