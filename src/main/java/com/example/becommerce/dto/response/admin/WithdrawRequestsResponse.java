package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WithdrawRequestsResponse {
    private final long pendingCount;
    private final List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private final String id;
        private final Technician technician;
        private final BigDecimal amount;
        private final String bankName;
        private final String accountNumber;
        private final LocalDateTime requestedAt;
        private final String status;
    }

    @Getter
    @Builder
    public static class Technician {
        private final String id;
        private final String fullName;
        private final String avatar;
    }
}
