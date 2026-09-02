package com.prepgenius.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProgressResponse {
    private Long totalInterviews;
    private Long completedInterviews;
    private Double averageScore;
    private Double highestScore;
    private Long totalQuestionsAttempted;
    private Long totalCorrectAnswers;
    private Long totalIncorrectAnswers;
    private Double averagePercentage;

    public ProgressResponse(Long totalInterviews, Long completedInterviews, Double averageScore, Double highestScore,
                            Long totalQuestionsAttempted, Long totalCorrectAnswers, Long totalIncorrectAnswers, Double averagePercentage) {
        this.totalInterviews = totalInterviews;
        this.completedInterviews = completedInterviews;
        this.averageScore = (averageScore != null) ? averageScore : 0.0;
        this.highestScore = (highestScore != null) ? highestScore : 0.0;
        this.totalQuestionsAttempted = totalQuestionsAttempted;
        this.totalCorrectAnswers = totalCorrectAnswers;
        this.totalIncorrectAnswers = totalIncorrectAnswers;
        this.averagePercentage = (averagePercentage != null) ? averagePercentage : 0.0;
    }
}
