package com.prepgenius.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPerformanceAnalysisResponse {

    private String aiFeedback;

    private List<String> strongTopics;

    private List<String> weakTopics;

    private String aiRecommendation;
}