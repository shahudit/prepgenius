package com.prepgenius.dto;

import com.prepgenius.model.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentInterviewResponse {
    private String interviewId;
    private String userName;
    private String categoryName;
    private String companyName;
    private Difficulty difficulty;
    private Double percentage;
    private LocalDateTime completedAt;
}
