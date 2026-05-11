package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RecentOrdersResponse {
    private final List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private final String id;
        private final Person customer;
        private final Person technician;
        private final String serviceName;
        private final String status;
        private final LocalDateTime scheduledAt;
        private final BigDecimal amount;
    }

    @Getter
    @Builder
    public static class Person {
        private final String fullName;
    }
}
