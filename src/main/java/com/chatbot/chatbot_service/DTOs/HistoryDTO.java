package com.chatbot.chatbot_service.DTOs;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HistoryDTO {
    private String question;
    private LocalDateTime qdate;
    private String answer;
    private int model;
}
