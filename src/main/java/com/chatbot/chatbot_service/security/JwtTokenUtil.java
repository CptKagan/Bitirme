package com.chatbot.chatbot_service.security;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {
    private final String secret = "3viXNPoEKeapyXbHoTnL+/oDjbun6XC3gAu8PKbIt98=";
    private final long expiration = 36000000;

    public String generateToken(String userName){
        return Jwts.builder()
                   .setSubject(userName) // Saklanacak Ana Bilgi
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis()+expiration))
                   .signWith(SignatureAlgorithm.HS256, secret)
                   .compact();
    }

    public Claims validateToken(String token){ 
        try{
            return Jwts.parser()
                       .setSigningKey(secret) // İmza kontrol edilir
                       .parseClaimsJws(token)
                       .getBody();
        } catch (Exception e){
            return null; // Hatalı token
        }
    }
}
