package com.chatbot.chatbot_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatbot.chatbot_service.models.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session,Long>{
    
}
