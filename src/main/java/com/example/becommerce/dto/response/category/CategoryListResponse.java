package com.example.becommerce.dto.response.category;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryListResponse {
    private final List<CategoryResponse> items;
}
