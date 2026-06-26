package com.example.becommerce.dto.request.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResolveReportRequest {
    private String status; // open, investigating, resolved, dismissed
    private String note;
    private Boolean lockTechnician;
    private String lockReason;
}
