package com.prepgenius.dto;

import com.prepgenius.model.Difficulty;
import com.prepgenius.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionForInterviewResponse {
    private String id;
    private String questionText;
    private QuestionType type;
    private Difficulty difficulty;
    private String categoryId;
    private List<String> companyIds;
    private List<String> options;
}
