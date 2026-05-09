package com.example.becommerce.service;

import com.example.becommerce.dto.response.QuoteResponse;

public interface QuoteService {

    QuoteResponse acceptQuote(Long quoteId);

    QuoteResponse rejectQuote(Long quoteId);
}
