package com.example.becommerce.dto.response.technician;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilityResponse {

    private String id;
    private Boolean isAvailable;
    private LocalDateTime updatedAt;
}
