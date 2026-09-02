package com.prepgenius.controller;

import com.prepgenius.dto.*;
import com.prepgenius.model.Interview;
import com.prepgenius.repository.UserRepository;
import com.prepgenius.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final UserRepository userRepository;

    private String getUserId(Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @PostMapping("/start")
    public ResponseEntity<StartInterviewResponse> startInterview(
            @Valid @RequestBody StartInterviewRequest request,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                interviewService.startInterview(request, userId)
        );
    }

    @PostMapping("/practice/start")
    public ResponseEntity<StartInterviewResponse> startPractice(
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                interviewService.startPractice(userId)
        );
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<SubmitAnswerResponse> submitAnswer(
            @PathVariable String id,
            @Valid @RequestBody SubmitAnswerRequest request,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                interviewService.submitAnswer(id, request, userId)
        );
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Interview> completeInterview(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                interviewService.completeInterview(id, userId)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<Page<InterviewHistoryResponse>> getHistory(
            Pageable pageable,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                interviewService.getHistory(userId, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewDetailsResponse> getInterviewDetails(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                interviewService.getInterviewDetails(id, userId)
        );
    }

    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> getProgress(
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                interviewService.getProgress(userId)
        );
    }
}