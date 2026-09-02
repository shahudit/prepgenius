package com.prepgenius.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {
    @NotBlank(message = "Question ID is required")
    private String questionId;

    private String userAnswer;

    private Integer selectedOptionIndex;
}
