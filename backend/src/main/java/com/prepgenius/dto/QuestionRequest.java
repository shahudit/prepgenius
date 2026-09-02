package com.prepgenius.dto;

import com.prepgenius.model.Difficulty;
import com.prepgenius.model.InterviewMode;
import com.prepgenius.model.QuestionStatus;
import com.prepgenius.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    @NotNull(message = "Difficulty is required")
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

    @NotNull(message = "Question status is required")
    private QuestionStatus status;

    private String sourceType;

    private String sourceReference;

    private String sourceUrl;
}
