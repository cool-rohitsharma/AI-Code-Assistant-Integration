package com.example.ai_service_integration.AiIntegration.service;

import com.example.ai_service_integration.AiIntegration.dto.CompletionResponse;
import reactor.core.publisher.Mono;

public interface AiService {
    Mono<CompletionResponse> getAiCompletion(String prompt);
}