package com.chatbot.chatbot_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordDTO {
    @NotBlank(message = "Old password must not be blank!")
    @Size(min = 6, message = "Old password must atleast 6 characters long!")
    private String oldPassword;

    @Size(min = 6, message = "New password must atleast 6 characters long!")
    @NotBlank(message = "New password must not be blank!")
    private String newPassword;
}
