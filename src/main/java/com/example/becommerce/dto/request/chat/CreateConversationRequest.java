package com.example.becommerce.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {

    /** Partner technician's user code, e.g. TECH-001. */
    @NotBlank(message = "technicianId không được trống")
    private String technicianId;

    /** Optional — link the chat to an existing order. */
    private String orderId;
}
