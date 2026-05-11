package com.example.becommerce.dto.response.verification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationCreatedResponse {

    private String id;
    private String technicianId;
    private String status;
    private LocalDateTime submittedAt;
}
