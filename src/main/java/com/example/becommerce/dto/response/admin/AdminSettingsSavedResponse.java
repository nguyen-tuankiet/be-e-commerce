package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminSettingsSavedResponse {
    private final String message;
    private final LocalDateTime updatedAt;
}
