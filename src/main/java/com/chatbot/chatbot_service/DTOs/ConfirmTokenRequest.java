package com.chatbot.chatbot_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConfirmTokenRequest {
    @NotBlank(message = "Token must not be blank!")
    private String token;
}
