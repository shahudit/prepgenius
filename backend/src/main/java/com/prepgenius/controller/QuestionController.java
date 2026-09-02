package com.prepgenius.controller;

import com.prepgenius.dto.QuestionForInterviewResponse;
import com.prepgenius.dto.QuestionResponse;
import com.prepgenius.model.Difficulty;
import com.prepgenius.model.InterviewMode;
import com.prepgenius.model.QuestionStatus;
import com.prepgenius.model.QuestionType;
import com.prepgenius.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

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
    public ResponseEntity<QuestionForInterviewResponse> getQuestionForInterview(@PathVariable String id) {
        return ResponseEntity.ok(questionService.getQuestionForInterview(id));
    }
}
