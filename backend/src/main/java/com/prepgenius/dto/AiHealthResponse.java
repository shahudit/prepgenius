package com.prepgenius.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiHealthResponse {
    private boolean configured;
    private boolean reachable;
    private String model;
    private String message;
}
