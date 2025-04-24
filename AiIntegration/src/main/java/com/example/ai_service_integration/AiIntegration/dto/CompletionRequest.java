package com.example.ai_service_integration.AiIntegration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletionRequest {
    @NotBlank(message = "Prompt cannot be empty")
    private String prompt;
}
