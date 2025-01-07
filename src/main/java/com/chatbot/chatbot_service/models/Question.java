package com.chatbot.chatbot_service.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Question {
    @Id
    @SequenceGenerator(name = "question_seq", sequenceName = "question_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_seq")
    private Long id;

    private LocalDateTime timeStamp;

    @Column(columnDefinition = "TEXT", length = 200)
    private String question;

    @Column(columnDefinition = "TEXT", length = 400, nullable = true)
    private String response;

    private int modelId;

    private int chunkSize = 500;

    private int chunkOverlap = 100;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = true)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "chatsession_id", referencedColumnName = "id", nullable = true)
    private ChatSession chatSession;
}
