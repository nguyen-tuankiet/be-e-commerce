package com.example.becommerce.dto.response.admin;

import com.example.becommerce.dto.response.PagedResponse;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommissionWalletsResponse {
    private final List<Item> content;
    private final PagedResponse.PaginationMeta pagination;

    @Getter
    @Builder
    public static class Item {
        private final Long technicianId;
        private final String technicianName;
        private final BigDecimal walletBalance;
        private final String walletStatus;
        private final BigDecimal totalCommissionPaid;
        private final LocalDateTime lastOrderAt;
        private final Boolean locked;
    }
}
