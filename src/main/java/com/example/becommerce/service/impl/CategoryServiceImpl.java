package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.CategoryMapper;
import com.example.becommerce.dto.request.category.CategoryStatusRequest;
import com.example.becommerce.dto.request.category.CategoryUpsertRequest;
import com.example.becommerce.dto.response.category.CategoryDeleteResponse;
import com.example.becommerce.dto.response.category.CategoryListResponse;
import com.example.becommerce.dto.response.category.CategoryResponse;
import com.example.becommerce.entity.Category;
import com.example.becommerce.entity.enums.CategoryPriority;
import com.example.becommerce.entity.enums.VisibilityStatus;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.CategoryRepository;
import com.example.becommerce.service.CategoryService;
import com.example.becommerce.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public CategoryListResponse getCategories(String status) {
        List<Category> categories;
        if (StringUtils.hasText(status) && !"all".equalsIgnoreCase(status)) {
            categories = categoryRepository.findByStatusAndDeletedFalseOrderByPriorityDescCreatedAtDesc(parseStatus(status));
        } else {
            categories = categoryRepository.findByDeletedFalseOrderByPriorityDescCreatedAtDesc();
        }
        return CategoryListResponse.builder()
                .items(categories.stream().map(categoryMapper::toResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryUpsertRequest request) {
        Category category = Category.builder()
                .code(generateCategoryCode())
                .title(request.getTitle().trim())
                .description(trimToNull(request.getDescription()))
                .priority(parsePriority(request.getPriority()))
                .status(parseStatus(request.getStatus()))
                .build();
        if (request.getIcon() != null && !request.getIcon().isEmpty()) {
            category.setIconUrl(fileStorageService.storeImage(request.getIcon(), "categories").getUrl());
        }
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, CategoryUpsertRequest request) {
        Category category = getCategory(id);
        category.setTitle(request.getTitle().trim());
        category.setDescription(trimToNull(request.getDescription()));
        category.setPriority(parsePriority(request.getPriority()));
        category.setStatus(parseStatus(request.getStatus()));
        if (request.getIcon() != null && !request.getIcon().isEmpty()) {
            category.setIconUrl(fileStorageService.storeImage(request.getIcon(), "categories").getUrl());
        }
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDeleteResponse deleteCategory(String id) {
        Category category = getCategory(id);
        category.setDeleted(true);
        categoryRepository.save(category);
        return CategoryDeleteResponse.builder()
                .id(category.getCode())
                .message("Xóa danh mục thành công")
                .build();
    }

    @Override
    @Transactional
    public CategoryResponse updateStatus(String id, CategoryStatusRequest request) {
        Category category = getCategory(id);
        category.setStatus(parseStatus(request.getStatus()));
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    private Category getCategory(String id) {
        return categoryRepository.findByCodeAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Không tìm thấy danh mục", org.springframework.http.HttpStatus.NOT_FOUND));
    }

    private String generateCategoryCode() {
        long next = categoryRepository.countByDeletedFalse() + 1;
        String code;
        do {
            code = "CAT-%03d".formatted(next++);
        } while (categoryRepository.existsByCode(code));
        return code;
    }

    private CategoryPriority parsePriority(String value) {
        try {
            return CategoryPriority.from(value);
        } catch (IllegalArgumentException ex) {
            throw AppException.badRequest(ErrorCode.INVALID_CATEGORY_PRIORITY, "Mức ưu tiên danh mục không hợp lệ");
        }
    }

    private VisibilityStatus parseStatus(String value) {
        try {
            return VisibilityStatus.from(value);
        } catch (IllegalArgumentException ex) {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS, "Trạng thái không hợp lệ");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
