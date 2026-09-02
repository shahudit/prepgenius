package com.prepgenius.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    private String id;

    private String categoryId;

    private InterviewMode interviewMode;

    private List<String> companyIds;

    private Difficulty difficulty;

    private QuestionType type;

    private String questionText;

    private List<String> options;

    private Integer correctIndex;

    private String expectedAnswer;

    private List<String> idealKeywords;

    private String evaluationCriteria;

    private String explanation;

    private Integer maxScore;

    private QuestionStatus status;

    private String sourceType;

    private String sourceReference;

    private String sourceUrl;

    private String createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
