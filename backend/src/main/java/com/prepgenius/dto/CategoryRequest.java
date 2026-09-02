package com.prepgenius.dto;

import com.prepgenius.model.InterviewMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    @NotNull(message = "Interview group is required")
    private InterviewMode group;

    private boolean active = true;
}
