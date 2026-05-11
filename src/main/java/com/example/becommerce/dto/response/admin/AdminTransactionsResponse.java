package com.example.becommerce.dto.response.admin;

import com.example.becommerce.dto.response.PagedResponse;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AdminTransactionsResponse {
    private final BigDecimal totalBalance;
    private final List<Item> items;
    private final PagedResponse.PaginationMeta pagination;

    @Getter
    @Builder
    public static class Item {
        private final String id;
        private final String time;
        private final String date;
        private final Partner partner;
        private final String type;
        private final BigDecimal amount;
        private final String status;
    }

    @Getter
    @Builder
    public static class Partner {
        private final String name;
        private final String area;
    }
}
