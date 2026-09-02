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
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalCompanies;
    private long totalCategories;
    private long totalInterviews;
    private double averageScore;
    private List<RecentInterviewResponse> recentInterviews;
}
