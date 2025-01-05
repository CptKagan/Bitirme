package com.chatbot.chatbot_service.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
        private final AuthenticationManager authenticationManager;

    public AuthService(AuthenticationManager authenticationMaanger){
        this.authenticationManager = authenticationMaanger;
    }

    public void authenticate(String userName, String password){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password)); // Giriş yapmaya çalışan kişinin kimliğinin doğrulanması
    }
}
