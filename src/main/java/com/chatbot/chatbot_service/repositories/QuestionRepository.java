package com.chatbot.chatbot_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatbot.chatbot_service.models.Account;
import com.chatbot.chatbot_service.models.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long>{
    @Query("SELECT q FROM Question q WHERE q.account = :account ORDER BY q.timeStamp ASC")
    List<Question> findByAccount(@Param("account") Account account);

}
