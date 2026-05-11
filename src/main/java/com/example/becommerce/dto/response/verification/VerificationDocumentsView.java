package com.example.becommerce.dto.response.verification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationDocumentsView {

    private String idFront;
    private String idBack;
    private String portrait;
    private String certificate;
}
