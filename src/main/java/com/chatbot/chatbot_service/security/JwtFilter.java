package com.chatbot.chatbot_service.security;


import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization"); // Header kontrolü

        // Skip JWT processing for specific paths
        // if (path.equals("/deneme") || path.startsWith("/register")) {
        //     filterChain.doFilter(request, response);
        //     return;
        // }

        if(header == null || !header.startsWith("Bearer ")){ // Bearer ile Başlıyor mu?
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7); // 7. karakterden itibaren tokenı al (Bearer )
        Claims claims = jwtTokenUtil.validateToken(token); // Validate Token fonksiyonunu çağır 

        if(claims != null && SecurityContextHolder.getContext().getAuthentication() == null){ // token doğru mu? && kullanıcı zaten doğrulanmış mı?
            String userName = claims.getSubject(); // Token içindeki username alınır

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER")); // Rol verilir

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userName, null, authorities); // Rol atanır
            SecurityContextHolder.getContext().setAuthentication(authToken); // SecurityContextHolder içerisine yerleştir
        }
        filterChain.doFilter(request, response);
    }
}
