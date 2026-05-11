package com.example.becommerce.service;

import com.example.becommerce.dto.request.verification.CreateVerificationRequest;
import com.example.becommerce.dto.request.verification.ReviewVerificationRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.verification.VerificationCreatedResponse;
import com.example.becommerce.dto.response.verification.VerificationDetailResponse;
import com.example.becommerce.dto.response.verification.VerificationListItemResponse;
import com.example.becommerce.dto.response.verification.VerificationReviewResponse;

public interface VerificationService {

    PagedResponse<VerificationListItemResponse> list(String status, String keyword, int page, int limit);

    VerificationCreatedResponse submit(CreateVerificationRequest request);

    VerificationDetailResponse get(String code);

    VerificationReviewResponse review(String code, ReviewVerificationRequest request);
}
