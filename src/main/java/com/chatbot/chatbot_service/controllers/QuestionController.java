package com.chatbot.chatbot_service.controllers;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.chatbot.chatbot_service.DTOs.ChatSessionNameDTO;
import com.chatbot.chatbot_service.DTOs.QuestionRequest;
import com.chatbot.chatbot_service.services.QuestionService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;




@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/question")
    public ResponseEntity<?> askQuestion(@Valid @RequestBody QuestionRequest questionRequest, 
                                        BindingResult bindingResult, 
                                        Authentication authentication) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }

        if (authentication == null) {
            // Giriş yapmayan kullanıcılar için
            if (questionRequest.getChatSessionId() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Chat session ID is not required for guest users.");
            }
            return questionService.askQuestion(questionRequest);
        } else {
            // Giriş yapan kullanıcılar için
            if (questionRequest.getChatSessionId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Chat session ID is required for logged-in users.");
            }
            return questionService.askQuestionLoggedIn(questionRequest, authentication);
        }
    }

    @PostMapping("/chatsession")
    public ResponseEntity<?> createChatSession(Authentication authentication, @RequestBody ChatSessionNameDTO chatSessionNameDTO) {
        if(authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return questionService.createChatSession(authentication, chatSessionNameDTO);
    }

    @GetMapping("/getchatsessions")
    public ResponseEntity<?> getChatSessions(Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return questionService.getChatSessions(authentication);
    }

    @GetMapping("/getchatsessions/{id}")
    public ResponseEntity<?> getSingleChatSession(@PathVariable Long id, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return questionService.getSingleChatSession(id, authentication);
    }
    
    
    
}
