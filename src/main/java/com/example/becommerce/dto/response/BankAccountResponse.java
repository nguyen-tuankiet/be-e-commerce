package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Bank account response.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankAccountResponse {

    private String id;
    private String bankName;
    private String accountNumber;
    private String accountOwner;
    private boolean isDefault;
    private LocalDateTime createdAt;
}

