package com.prepgenius.controller;

import com.prepgenius.ai.GeminiClientInterface;
import com.prepgenius.dto.AiHealthResponse;
import com.prepgenius.dto.AnswerEvaluationResponse;
import com.prepgenius.dto.EvaluateAnswerRequest;
import com.prepgenius.dto.GenerateQuestionsRequest;
import com.prepgenius.dto.QuestionResponse;
import com.prepgenius.dto.StudyMaterialRequest;
import com.prepgenius.dto.StudyMaterialResponse;
import com.prepgenius.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final QuestionService questionService;
    private final com.prepgenius.ai.GeminiService geminiService;
    private final GeminiClientInterface geminiClient;

    @GetMapping("/health")
    public ResponseEntity<AiHealthResponse> health() {
        if (!geminiClient.isConfigured()) {
            return ResponseEntity.ok(AiHealthResponse.builder()
                    .configured(false)
                    .reachable(false)
                    .model(geminiClient.getModel())
                    .message("GEMINI_API_KEY is not set. Add it to backend/.env and restart the app.")
                    .build());
        }

        try {
            String response = geminiClient.generateContent(
                    "Return ONLY this exact JSON object, nothing else: {\"status\":\"ok\"}");

            return ResponseEntity.ok(AiHealthResponse.builder()
                    .configured(true)
                    .reachable(true)
                    .model(geminiClient.getModel())
                    .message("Gemini responded: " + (response == null ? "" : response.trim()))
                    .build());
        } catch (Exception e) {
            log.warn("Gemini health check failed", e);
            return ResponseEntity.ok(AiHealthResponse.builder()
                    .configured(true)
                    .reachable(false)
                    .model(geminiClient.getModel())
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/questions/generate")
    public ResponseEntity<List<QuestionResponse>> generateQuestions(@Valid @RequestBody GenerateQuestionsRequest request) {
        return ResponseEntity.ok(questionService.getOrGenerateQuestions(
                request.getCompanyId(),
                request.getCategoryId(),
                request.getDifficulty(),
                request.getQuestionType(),
                request.getNumberOfQuestions()
        ));
    }

    @PostMapping("/answers/evaluate")
    public ResponseEntity<AnswerEvaluationResponse> evaluateAnswer(@Valid @RequestBody EvaluateAnswerRequest request) {
        return ResponseEntity.ok(geminiService.evaluateAnswer(
                request.getQuestionText(),
                request.getExpectedAnswer(),
                request.getIdealKeywords(),
                request.getUserAnswer()
        ));
    }

    @PostMapping("/study-material")
    public ResponseEntity<StudyMaterialResponse> generateStudyMaterial(@Valid @RequestBody StudyMaterialRequest request) {
        return ResponseEntity.ok(geminiService.generateStudyMaterial(
                request.getWeakTopics(),
                request.getCompanyName() != null && !request.getCompanyName().isBlank()
                        ? request.getCompanyName()
                        : "General",
                request.getCategoryName() != null && !request.getCategoryName().isBlank()
                        ? request.getCategoryName()
                        : "General"
        ));
    }
}
