package com.chatbot.chatbot_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chatbot.chatbot_service.models.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByUserName(String userName);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    Optional<Account> findByPasswordResetToken(String resetToken);
}