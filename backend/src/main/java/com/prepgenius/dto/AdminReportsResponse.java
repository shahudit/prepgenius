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
public class AdminReportsResponse {
    private long totalInterviews;
    private long totalCompanies;
    private long totalCategories;
    private List<CategoryStat> byCategory;
    private List<CompanyStat> byCompany;
    private List<TopLearnerStat> topLearners;
}
