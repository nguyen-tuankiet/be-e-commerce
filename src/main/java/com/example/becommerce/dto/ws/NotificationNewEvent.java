package com.example.becommerce.dto.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Pushed to {@code /queue/notifications.{userCode}} when a new
 * notification is created for that user.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationNewEvent {

    @Builder.Default
    private final String event = "notification:new";

    private String id;
    private String type;
    private String title;
    private String body;
    private Map<String, Object> data;
}
