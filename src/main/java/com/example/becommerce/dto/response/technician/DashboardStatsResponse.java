package com.example.becommerce.dto.response.technician;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardStatsResponse {
    private long totalOrders;
    private long completedOrders;
    private long weeklyEarnings;
}