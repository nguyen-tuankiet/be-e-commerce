package com.example.becommerce.dto.response.category;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDeleteResponse {
    private final String id;
    private final String message;
}
