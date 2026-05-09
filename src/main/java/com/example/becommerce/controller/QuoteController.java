package com.example.becommerce.controller;

import com.example.becommerce.constant.ApiConstant;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.QuoteResponse;
import com.example.becommerce.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstant.QUOTE_BASE)
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<QuoteResponse>> acceptQuote(@PathVariable Long id) {
        QuoteResponse data = quoteService.acceptQuote(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<QuoteResponse>> rejectQuote(@PathVariable Long id) {
        QuoteResponse data = quoteService.rejectQuote(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
