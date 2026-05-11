package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class RevenueStatsResponse {
    private final String range;
    private final List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private final String label;
        private final BigDecimal value;
        private final LocalDate date;
    }
}
