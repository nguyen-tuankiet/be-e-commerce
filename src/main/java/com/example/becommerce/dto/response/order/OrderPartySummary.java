package com.example.becommerce.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Minimal user info embedded inside Order responses.
 * Avoids leaking sensitive fields and keeps payloads compact for listings.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderPartySummary {

    private String id;
    private String fullName;
    private String phone;
    private String avatar;
    private BigDecimal rating;
}
