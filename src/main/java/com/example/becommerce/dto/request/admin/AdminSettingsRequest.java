package com.example.becommerce.dto.request.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminSettingsRequest {
    @Valid
    @NotNull
    private General general;

    @Valid
    @NotNull
    private Billing billing;

    @Valid
    @NotNull
    private Notifications notifications;

    @Valid
    @NotNull
    private Operations operations;

    @Getter
    @Setter
    public static class General {
        @NotBlank
        private String appName;
        @NotBlank
        private String hotline;
        @Email
        @NotBlank
        private String email;
    }

    @Getter
    @Setter
    public static class Billing {
        @NotNull
        @DecimalMin("0")
        @DecimalMax("100")
        private BigDecimal platformFeePercent;
        @NotNull
        @DecimalMin("0")
        @DecimalMax("100")
        private BigDecimal vatPercent;
    }

    @Getter
    @Setter
    public static class Notifications {
        private boolean newOrder;
        private boolean customerEmail;
        private boolean weeklyRevenue;
        private boolean securityAlert;
    }

    @Getter
    @Setter
    public static class Operations {
        private boolean requireManualReview;
        private boolean technicianAutoPause;
        private boolean incidentEscalation;
    }
}
