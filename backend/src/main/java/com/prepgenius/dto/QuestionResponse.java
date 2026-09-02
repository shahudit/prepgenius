package com.prepgenius.dto;

import com.prepgenius.model.Difficulty;
import com.prepgenius.model.InterviewMode;
import com.prepgenius.model.QuestionStatus;
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
public class QuestionResponse {
    private String id;
    private String questionText;
    private QuestionType type;
    private Difficulty difficulty;
    private String categoryId;
    private InterviewMode interviewMode;
    private List<String> companyIds;
    private List<String> options;
    private Integer correctIndex;
    private String expectedAnswer;
    private List<String> idealKeywords;
    private String explanation;
    private String evaluationCriteria;
    private Integer maxScore;
    private QuestionStatus status;
    private String sourceType;
    private String sourceReference;
    private String sourceUrl;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
