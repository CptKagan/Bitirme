package com.chatbot.chatbot_service.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionRequest {
    @NotBlank(message = "Question must not be blank!")
    @Size(min = 20, max = 200, message = "Question must be atleast 20 characters long, and at max 200 characters long!")
    private String question;

    private int model;

    private Long chatSessionId;
}
