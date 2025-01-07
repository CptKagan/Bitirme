package com.chatbot.chatbot_service.DTOs;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionDTO {
    private Long id;
    private String question;
    private String response;
    private int model;
    private LocalDateTime timeStamp;
}
