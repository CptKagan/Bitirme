package com.chatbot.chatbot_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank(message = "New password must not be blank!")
    @Size(min = 6, message = "Password must be at least 6 characters long!")
    private String newPassword;
}
