package com.prepgenius.dto;

import com.prepgenius.model.Difficulty;
import com.prepgenius.model.InterviewMode;
import com.prepgenius.model.QuestionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartInterviewRequest {

    @NotBlank(message = "Company ID is required")
    private String companyId;

    private String categoryId;

    @NotNull(message = "Interview type is required")
    private InterviewMode interviewMode;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    @Min(value = 1, message = "Minimum 1 question required")
    @Max(value = 20, message = "Maximum 20 questions allowed")
    private int numberOfQuestions;
}