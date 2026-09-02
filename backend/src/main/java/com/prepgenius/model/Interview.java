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

@Document(collection = "interviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    private String id;

    private String userId;

    private String companyId;

    private String categoryId;

    private InterviewMode interviewMode;

    private Difficulty difficulty;

    private QuestionType questionType;

    private Integer numberOfQuestions;

    private List<String> questionIds;

    private Integer answeredQuestions;

    private Integer correctAnswers;

    private Integer incorrectAnswers;

    private Integer skippedQuestions;

    private Double totalScore;

    private Double percentage;

    private InterviewStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationSeconds;

    private String aiFeedback;

    private List<String> strongTopics;

    private List<String> weakTopics;

    private String aiRecommendation;

    private LocalDateTime aiAnalysisGeneratedAt;

    @CreatedDate
    private LocalDateTime createdAt;
}