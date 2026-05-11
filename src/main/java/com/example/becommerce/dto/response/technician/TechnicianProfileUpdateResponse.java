package com.example.becommerce.dto.response.technician;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Compact echo of fields touched by PATCH /api/technicians/:id/profile.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnicianProfileUpdateResponse {

    private String id;
    private String fullName;
    private String bio;
    private List<String> skills;
    private List<String> areas;
    private LocalDateTime updatedAt;
}
