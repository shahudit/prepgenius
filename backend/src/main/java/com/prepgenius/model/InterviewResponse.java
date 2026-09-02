package com.prepgenius.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "interview_responses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {
    @Id
    private String id;

    private String interviewId;

    private String questionId;

    private String userAnswer;

    private boolean isCorrect;

    private Double score;

    private Integer maxScore;

    private String aiFeedback;

    private List<String> strengths;

    private List<String> weaknesses;

    private List<String> improvementSuggestions;

    private List<String> matchedKeywords;

    private LocalDateTime answeredAt;
}
