package com.example.becommerce.dto.request.report;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {

    /** snake_case API value, e.g. "extra_fee" — parsed by ReportReason.from(...) */
    @NotBlank(message = "Lý do report không được trống")
    private String reason;

    @NotBlank(message = "Mô tả không được trống")
    private String description;

    private List<String> evidenceImages;
}
