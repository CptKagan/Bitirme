package com.chatbot.chatbot_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatbot.chatbot_service.models.ChatSession;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession,Long> {
    List<ChatSession> findByAccountId(Long accountId);
    Optional<ChatSession> findByIdAndAccountId(Long id, Long accountId);
}
