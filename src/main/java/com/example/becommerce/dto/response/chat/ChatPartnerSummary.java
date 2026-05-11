package com.example.becommerce.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatPartnerSummary {

    private String id;
    private String fullName;
    private String avatar;

    /** Best-effort online flag; defaults to false until presence service exists. */
    private Boolean isOnline;
}
