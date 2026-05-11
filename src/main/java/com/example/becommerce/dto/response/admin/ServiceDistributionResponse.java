package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ServiceDistributionResponse {
    private final List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private final String name;
        private final BigDecimal percentage;
        private final String color;
    }
}
