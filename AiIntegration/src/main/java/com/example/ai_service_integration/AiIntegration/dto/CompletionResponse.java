package com.example.ai_service_integration.AiIntegration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletionResponse {
    private String completion;
    private String model; // Optional: Include model info
}
