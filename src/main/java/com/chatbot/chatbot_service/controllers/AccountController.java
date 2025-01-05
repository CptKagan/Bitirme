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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.chatbot.chatbot_service.DTOs.ChangePasswordDTO;
import com.chatbot.chatbot_service.DTOs.ConfirmTokenRequest;
import com.chatbot.chatbot_service.DTOs.ForgetPassword;
import com.chatbot.chatbot_service.DTOs.LoginRequest;
import com.chatbot.chatbot_service.DTOs.ProfileUpdate;
import com.chatbot.chatbot_service.DTOs.RegisterRequest;
import com.chatbot.chatbot_service.DTOs.ResetPasswordRequest;
import com.chatbot.chatbot_service.security.JwtTokenUtil;
import com.chatbot.chatbot_service.services.AccountService;
import com.chatbot.chatbot_service.services.AuthService;

import jakarta.validation.Valid;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error -> 
                System.out.println("Validation Error: " + error.getField() + " - " + error.getDefaultMessage())
            );
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }        
        System.out.println("Received RegisterRequest: " + registerRequest);
        return accountService.save(registerRequest);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        try{
            authService.authenticate(loginRequest.getUserName(), loginRequest.getPassword());
            String token = jwtTokenUtil.generateToken(loginRequest.getUserName());
            accountService.saveSession(loginRequest.getUserName(), token);
            return ResponseEntity.ok(Map.of("token",token));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @GetMapping("/deneme")
    public ResponseEntity<?> deneme() {
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/forgetpassword")
    public ResponseEntity<?> forgetPassword(@Valid @RequestBody ForgetPassword forgetPassword, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        return accountService.forgetPassword(forgetPassword);
    }
    
    @PostMapping("/reset-password/{token}")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest, BindingResult bindingResult, @PathVariable String token) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        return accountService.resetPassword(resetPasswordRequest, token);
    }

    @PostMapping("/confirmtoken")
    public ResponseEntity<?> confirmPasswordResetToken(@Valid @RequestBody ConfirmTokenRequest confirmTokenRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        return accountService.confirmPasswordResetToken(confirmTokenRequest);
    }    

    @GetMapping("/profile")
    public ResponseEntity<?> profileView(Authentication authentication) {
        if(authentication == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in!");
        }
        return accountService.profileView(authentication);
    }

    @PostMapping("/profile/update")
    public ResponseEntity<?> profileUpdate(@Valid @RequestBody ProfileUpdate profileUpdate, Authentication authentication, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        if(authentication == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in!");
        }
        return accountService.profileUpdate(profileUpdate, authentication);
    }
    
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if(authentication == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in!");
        }
        return accountService.getUserInfo(authentication);
    }
    
    @GetMapping("/namesurname")
    public ResponseEntity<?> getNameSurname(Authentication authentication) {
        if(authentication == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in!");
        }
        return accountService.getNameSurname(authentication);
    }

    @PostMapping("/changepassword")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO, BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        if(authentication == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in!");
        }
        return accountService.changePassword(changePasswordDTO, authentication);
    }
}
