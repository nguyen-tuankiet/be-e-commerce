package com.example.becommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearchRequest {

    private String status;
    private String keyword;
    private Long customerId;
    private Long technicianId;
    private int page = 1;
    private int limit = 10;
}
