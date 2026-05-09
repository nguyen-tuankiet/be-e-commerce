package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Wrapper for bank account list responses.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankAccountListResponse {

    private List<BankAccountResponse> items;
}

