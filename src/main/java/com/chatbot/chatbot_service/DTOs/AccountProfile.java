package com.chatbot.chatbot_service.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountProfile {
    private String firstName;
    private String lastName;
    private String email;
}
