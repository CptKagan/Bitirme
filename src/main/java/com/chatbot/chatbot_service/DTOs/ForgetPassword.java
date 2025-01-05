package com.chatbot.chatbot_service.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ForgetPassword {
    @NotBlank(message = "Username must not be blank!")
    private String userName;

    @NotBlank(message = "Email must not be blank!")
    @Email(message = "Please enter a valid email!")
    private String email;
}
