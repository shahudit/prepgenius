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
public class TopicStudyMaterial {

    private String topic;

    private String summary;

    private List<String> keyConcepts;

    private List<String> examples;

    private List<String> commonMistakes;

    private List<String> practiceTips;
}
