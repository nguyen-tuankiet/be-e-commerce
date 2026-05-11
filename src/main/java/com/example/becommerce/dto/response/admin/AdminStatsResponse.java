package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AdminStatsResponse {
    private final Metric totalRevenue;
    private final Metric totalProfit;
    private final Metric activeTechnicians;
    private final Metric ordersToday;

    @Getter
    @Builder
    public static class Metric {
        private final BigDecimal value;
        private final BigDecimal change;
        private final String changeDirection;
    }
}
