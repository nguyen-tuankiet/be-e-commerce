package com.example.becommerce.controller;

import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.quotation.AcceptQuotationResponse;
import com.example.becommerce.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Standalone quote endpoints — the FE accepts the quote outside
 * the conversation context via {@code /api/quotes/:id/accept}.
 */
@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<AcceptQuotationResponse>> accept(
            @PathVariable("id") String quotationCode) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.acceptQuotation(quotationCode)));
    }
}
