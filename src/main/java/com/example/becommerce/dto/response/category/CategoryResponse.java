package com.example.becommerce.dto.response.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {
    private final String id;
    private final String title;
    private final String description;
    private final String iconUrl;
    private final String priority;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
