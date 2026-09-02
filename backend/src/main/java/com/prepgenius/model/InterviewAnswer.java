package com.prepgenius.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "interview_answers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewAnswer {

    @Id
    private String id;

    private String interviewSessionId;

    private String questionId;

    private String userAnswer;

    private Integer selectedOptionIndex;

    private Double score;

    private Double maxScore;

    private Boolean correct;

    private String feedback;

    private List<String> matchedKeywords;

    private String correctAnswer;

    @org.springframework.data.annotation.Transient
    private String questionText;

    @CreatedDate
    private LocalDateTime answeredAt;
}