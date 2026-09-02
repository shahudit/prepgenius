package com.prepgenius.dto;

import com.prepgenius.model.Difficulty;
import com.prepgenius.model.InterviewStatus;
import com.prepgenius.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewHistoryResponse {

    private String interviewId;

    private String companyName;

    private String categoryName;

    private Difficulty difficulty;

    private QuestionType questionType;

    private Integer totalQuestions;

    private Double percentage;

    private Integer correctAnswers;

    private LocalDateTime completedAt;

    private InterviewStatus status;
}