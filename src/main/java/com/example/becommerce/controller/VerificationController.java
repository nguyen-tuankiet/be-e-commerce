package com.example.becommerce.controller;

import com.example.becommerce.dto.request.verification.CreateVerificationRequest;
import com.example.becommerce.dto.request.verification.ReviewVerificationRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.verification.VerificationCreatedResponse;
import com.example.becommerce.dto.response.verification.VerificationDetailResponse;
import com.example.becommerce.dto.response.verification.VerificationListItemResponse;
import com.example.becommerce.dto.response.verification.VerificationReviewResponse;
import com.example.becommerce.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verifications")
@RequiredArgsConstructor
@Validated
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<VerificationListItemResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(verificationService.list(status, keyword, page, limit)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VerificationCreatedResponse>> submit(
            @Valid @ModelAttribute CreateVerificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(verificationService.submit(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VerificationDetailResponse>> get(@PathVariable("id") String code) {
        return ResponseEntity.ok(ApiResponse.success(verificationService.get(code)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VerificationReviewResponse>> review(
            @PathVariable("id") String code,
            @Valid @RequestBody ReviewVerificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(verificationService.review(code, request)));
    }
}
