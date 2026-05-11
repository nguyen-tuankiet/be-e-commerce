package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.category.CategoryResponse;
import com.example.becommerce.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getCode())
                .title(category.getTitle())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .priority(category.getPriority().apiValue())
                .status(category.getStatus().apiValue())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
