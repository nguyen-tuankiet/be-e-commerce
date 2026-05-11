package com.example.becommerce.controller;

import com.example.becommerce.dto.request.category.CategoryStatusRequest;
import com.example.becommerce.dto.request.category.CategoryUpsertRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.category.CategoryDeleteResponse;
import com.example.becommerce.dto.response.category.CategoryListResponse;
import com.example.becommerce.dto.response.category.CategoryResponse;
import com.example.becommerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponse>> getCategories(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategories(status)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @ModelAttribute CategoryUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(categoryService.createCategory(request)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable String id,
            @Valid @ModelAttribute CategoryUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDeleteResponse>> deleteCategory(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.deleteCategory(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody CategoryStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateStatus(id, request)));
    }
}
