package com.example.becommerce.dto.response.technician;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChartDataResponse {
    private String label;
    private long value;
}
