package com.prepgenius.dto;

import com.prepgenius.model.Difficulty;
import com.prepgenius.model.InterviewStatus;
import com.prepgenius.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartInterviewResponse {
    private String interviewId;
    private String companyId;
    private String categoryId;
    private Difficulty difficulty;
    private QuestionType questionType;
    private int totalQuestions;
    private List<QuestionForInterviewResponse> questions;
    private LocalDateTime startedAt;
    private InterviewStatus status;

    @Builder.Default
    private boolean practiceMode = false;
    private List<String> focusTopics;
}
