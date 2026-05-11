package com.example.becommerce.service;

import com.example.becommerce.dto.request.quotation.CreateQuotationRequest;
import com.example.becommerce.dto.response.quotation.AcceptQuotationResponse;
import com.example.becommerce.dto.response.quotation.QuotationResponse;

public interface QuotationService {

    /** Technician creates a quote inside a conversation; also posts a quotation-typed message. */
    QuotationResponse createQuotation(String conversationCode, CreateQuotationRequest request);

    /** Customer accepts the quote; spawns a scheduled Order linked back to the quote. */
    AcceptQuotationResponse acceptQuotation(String quotationCode);
}
