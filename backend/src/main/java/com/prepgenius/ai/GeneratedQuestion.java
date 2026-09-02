package com.prepgenius.ai;

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
public class GeneratedQuestion {
    private String questionText;
    private QuestionType questionType;
    private Difficulty difficulty;
    private List<String> options;
    private Integer correctOptionIndex;
    private String expectedAnswer;
    private List<String> idealKeywords;
    private String explanation;
}
