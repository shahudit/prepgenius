package com.prepgenius.controller;

import com.prepgenius.dto.QuestionRequest;
import com.prepgenius.dto.QuestionResponse;
import com.prepgenius.model.Difficulty;
import com.prepgenius.model.InterviewMode;
import com.prepgenius.model.QuestionStatus;
import com.prepgenius.model.QuestionType;
import com.prepgenius.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserDetails adminDetails
    ) {
        return ResponseEntity.ok(questionService.createQuestion(request, adminDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<Page<QuestionResponse>> getQuestions(
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) InterviewMode interviewMode,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) QuestionStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(questionService.getQuestions(companyId, categoryId, interviewMode, difficulty, type, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable String id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
