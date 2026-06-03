package com.example.becommerce.dto.response.admin;

import com.example.becommerce.dto.response.PagedResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminTransactionsResponse {
    private final BigDecimal totalBalance;
    private final List<Item> items;
    private final PagedResponse.PaginationMeta pagination;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {
        private final String id;
        private final String transactionCode;
        private final String transactionType;
        private final BigDecimal amount;
        private final Long afterBalance;
        private final String walletStatus;
        private final String technicianName;
        private final String orderCode;
        private final String note;
        private final String actor;
        private final LocalDateTime createdAt;
        
        // Legacy fields for backward compatibility
        private final String time;
        private final String date;
        private final Partner partner;
        private final String type;
        private final String status;
    }

    @Getter
    @Builder
    public static class Partner {
        private final String name;
        private final String area;
    }
}
