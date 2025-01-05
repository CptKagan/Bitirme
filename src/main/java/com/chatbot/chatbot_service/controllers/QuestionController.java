package com.chatbot.chatbot_service.controllers;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.chatbot.chatbot_service.DTOs.QuestionRequest;
import com.chatbot.chatbot_service.services.QuestionService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/question")
    public ResponseEntity<?> askQuestion(@Valid @RequestBody QuestionRequest question, BindingResult bindingResult, Authentication authentication) {
    if (bindingResult.hasErrors()) {
        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return ResponseEntity.badRequest().body(errors);
    }

    if (authentication == null) {
        return questionService.askQuestion(question);
    } else {
        return questionService.askQuestionLoggedIn(question, authentication);
    }
}

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        return questionService.getHistory(authentication);
    }
}
