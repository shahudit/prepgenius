package com.prepgenius.dto;

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
public class EvaluateAnswerRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotBlank(message = "Expected answer is required")
    private String expectedAnswer;

    @NotNull(message = "Ideal keywords list is required")
    private List<String> idealKeywords;

    @NotBlank(message = "User answer is required")
    private String userAnswer;
}
