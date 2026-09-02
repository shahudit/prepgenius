package com.prepgenius.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyMaterialRequest {

    @NotEmpty(message = "At least one weak topic is required")
    private List<String> weakTopics;

    private String companyName;

    private String categoryName;

    private String difficulty;
}
