package com.chatbot.chatbot_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatSessionNameDTO {
    @NotBlank(message = "Chat name must not be blank!")
    private String chatName;
}
