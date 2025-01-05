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
import com.chatbot.chatbot_service.DTOs.QuestionRequest;
import com.chatbot.chatbot_service.models.Account;
import com.chatbot.chatbot_service.models.Question;
import com.chatbot.chatbot_service.repositories.QuestionRepository;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AccountService accountService;

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
            String questionText = questionRequest.getQuestion();
            String response = processQuestion(questionText, questionRequest.getModel());

            // Save the question and response to the database
            Question question = new Question();
            question.setQuestion(questionText);
            question.setModelId(questionRequest.getModel());
            question.setResponse(response);
            question.setTimeStamp(LocalDateTime.now());
            questionRepository.save(question);

            return ResponseEntity.ok(Map.of("answer", response,"model", question.getModelId()));

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
            String questionText = questionRequest.getQuestion();
            String response = processQuestion(questionText, questionRequest.getModel());

            // Save the question and response to the database with account information
            Question question = new Question();
            question.setQuestion(questionText);
            question.setModelId(questionRequest.getModel());
            question.setResponse(response);
            question.setAccount(accountService.getAccount(authentication));
            question.setTimeStamp(LocalDateTime.now());
            questionRepository.save(question);

            return ResponseEntity.ok(Map.of("answer", response,"model", question.getModelId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while processing your question.");
        }
    }

    public ResponseEntity<?> getHistory(Authentication authentication) {
    // Ensure the user is authenticated
    if (authentication == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in!");
    }

    // Get the account from the authentication object
    Account account = accountService.getAccount(authentication);

    // Fetch user's question history
    List<HistoryDTO> history = questionRepository.findByAccount(account).stream()
            .map(question -> {
                HistoryDTO historyDTO = new HistoryDTO();
                historyDTO.setModel(question.getModelId());
                historyDTO.setQuestion(question.getQuestion());
                historyDTO.setQdate(question.getTimeStamp());
                historyDTO.setAnswer(question.getResponse());
                return historyDTO;
            })
            .collect(Collectors.toList());

    // Handle empty history case
    if (history.isEmpty()) {
        return ResponseEntity.ok("You have no question history.");
    }

    return ResponseEntity.ok(history);
}

}
