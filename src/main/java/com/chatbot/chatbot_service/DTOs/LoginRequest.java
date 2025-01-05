package com.chatbot.chatbot_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username must not be blank!")
    private String userName;

    @NotBlank(message = "Password must not be blank!")
    @Size(min=6, message = "Password must be at least 6 characters!")
    private String password;
}
