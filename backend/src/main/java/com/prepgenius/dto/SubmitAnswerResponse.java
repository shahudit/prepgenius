package com.prepgenius.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerResponse {

    private String questionId;

    private Double score;

    private Double maxScore;

    private Boolean correct;

    private String feedback;

    private List<String> matchedKeywords;

    private Integer answeredQuestions;

    private Integer remainingQuestions;

    private Double currentScore;

    private Double currentPercentage;

    private String correctAnswer;
}