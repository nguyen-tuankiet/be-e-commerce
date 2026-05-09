package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Response for deleting a bank account.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankAccountDeleteResponse {

    private String message;
}

