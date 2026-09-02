package com.prepgenius.dto;

import com.prepgenius.model.Difficulty;
import com.prepgenius.model.Interview;
import com.prepgenius.model.InterviewAnswer;
import com.prepgenius.model.InterviewMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewDetailsResponse {
    private Interview session;
    private List<InterviewAnswer> answers;

    private String companyName;
    private String categoryName;
    private InterviewMode interviewMode;
    private Difficulty difficulty;
}
