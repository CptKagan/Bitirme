package com.chatbot.chatbot_service.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.chatbot.chatbot_service.DTOs.ChatSessionDTO;
import com.chatbot.chatbot_service.DTOs.ChatSessionNameDTO;
import com.chatbot.chatbot_service.DTOs.HistoryDTO;
import com.chatbot.chatbot_service.DTOs.QuestionDTO;
import com.chatbot.chatbot_service.DTOs.QuestionRequest;
import com.chatbot.chatbot_service.models.Account;
import com.chatbot.chatbot_service.models.ChatSession;
import com.chatbot.chatbot_service.models.Question;
import com.chatbot.chatbot_service.repositories.ChatSessionRepository;
import com.chatbot.chatbot_service.repositories.QuestionRepository;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    /**
     * Process the question by invoking the Python script.
     */
    private String processQuestion(String questionText, int model) throws Exception {
        String scriptPath;
        switch (model) {
            case 1:
                scriptPath = "C:\\Users\\mkaga\\OneDrive\\Masaüstü\\BITIRME\\LAMA3.1FIRSTRUN\\RAGwithbackend.py";
                break;
            case 2:
                scriptPath = "C:\\Users\\mkaga\\OneDrive\\Masaüstü\\BITIRME\\LAMA3.1FIRSTRUN\\RAGwithbackend2.py";
                break;
            default:
                throw new IllegalArgumentException("Invalid model number");
        }
        
        ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, questionText);

        // Execute the Python script
        Process process = processBuilder.start();

        // Read the output of the Python script
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // Wait for the process to complete
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python script execution failed with exit code " + exitCode);
        }

        return output.toString();
    }

    /**
     * Handle question asked by an anonymous user.
     */
    public ResponseEntity<?> askQuestion(QuestionRequest questionRequest) {
        try {
            // Soruyu işleme ve cevap oluşturma
            String questionText = questionRequest.getQuestion();
            String response = processQuestion(questionText, questionRequest.getModel());

            // Question nesnesini kaydet
            Question question = new Question();
            question.setQuestion(questionText);
            question.setModelId(questionRequest.getModel());
            question.setResponse(response);
            question.setTimeStamp(LocalDateTime.now());

            // Guest kullanıcılar için Account ve ChatSession ilişkisiz
            question.setChatSession(null);
            question.setAccount(null);

            questionRepository.save(question);

            // Yanıt döndür
            return ResponseEntity.ok(Map.of("answer", response, "model", question.getModelId()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while processing your question.");
        }
    }

    /**
     * Handle question asked by a logged-in user.
     */
    public ResponseEntity<?> askQuestionLoggedIn(QuestionRequest questionRequest, Authentication authentication) {
        try {

            if(questionRequest.getChatSessionId() == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat session ID is required.");
            }

            Optional<ChatSession> chatSessions = chatSessionRepository.findById(questionRequest.getChatSessionId());
            if(!chatSessions.isPresent()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat session not found.");
            }

            if (!chatSessions.get().getAccount().getId().equals(accountService.getAccount(authentication).getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            
            ChatSession chatSession = chatSessions.get();

            String questionText = questionRequest.getQuestion();
            String response = processQuestion(questionText, questionRequest.getModel());

            // Save the question and response to the database with account information
            Question question = new Question();
            question.setQuestion(questionText);
            question.setModelId(questionRequest.getModel());
            question.setResponse(response);
            question.setAccount(accountService.getAccount(authentication));
            question.setChatSession(chatSession);
            question.setTimeStamp(LocalDateTime.now());
            questionRepository.save(question);

            return ResponseEntity.ok(Map.of("answer", response,"model", question.getModelId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while processing your question.");
        }
    }

    public ResponseEntity<?> createChatSession(Authentication authentication, ChatSessionNameDTO chatSessionNameDTO) {
        Account account = accountService.getAccount(authentication);
        ChatSession chatSession = new ChatSession();
        chatSession.setChatName(chatSessionNameDTO.getChatName());
        chatSession.setAccount(account);
        chatSession.setCreatedAt(LocalDateTime.now());

        chatSessionRepository.save(chatSession);
        return ResponseEntity.ok(chatSession.getId());
    }

    public ResponseEntity<?> getChatSessions(Authentication authentication) {
        Account account = accountService.getAccount(authentication);
        List<ChatSession> chatSessions = chatSessionRepository.findByAccountId(account.getId());
        List<ChatSessionDTO> chatSessionDTOs = chatSessions.stream()
                .map(chatSession -> {
                    ChatSessionDTO chatSessionDTO = new ChatSessionDTO();
                    chatSessionDTO.setId(chatSession.getId());
                    chatSessionDTO.setChatName(chatSession.getChatName());
                    chatSessionDTO.setCreatedAt(chatSession.getCreatedAt());
                    return chatSessionDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(chatSessionDTOs);
    }

    public ResponseEntity<?> getSingleChatSession(Long id, Authentication authentication) {
        Account account = accountService.getAccount(authentication);
        Optional<ChatSession> chatSessions = chatSessionRepository.findByIdAndAccountId(id, account.getId());
        if(!chatSessions.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat session not found.");
        }
        if(chatSessions.get().getAccount().getId() != account.getId()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        ChatSession chatSession = chatSessions.get();
        List<QuestionDTO> questions = chatSession.getQuestions().stream()
                .map(question -> {
                    QuestionDTO questionDTO = new QuestionDTO();
                    questionDTO.setQuestion(question.getQuestion());
                    questionDTO.setResponse(question.getResponse());
                    questionDTO.setTimeStamp(question.getTimeStamp());
                    questionDTO.setModel(question.getModelId());
                    questionDTO.setId(question.getId());
                    return questionDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(questions);
    }


}
