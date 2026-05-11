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
public class SendMessageRequest {

    /** "text" | "image" — quotation messages are produced via /quotes. */
    private String type;

    @NotBlank(message = "Nội dung tin nhắn không được trống")
    private String content;
}
