package com.chatbot.chatbot_service.DTOs;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatSessionDTO {
    private Long id;
    private String chatName;
    private LocalDateTime createdAt;
}
