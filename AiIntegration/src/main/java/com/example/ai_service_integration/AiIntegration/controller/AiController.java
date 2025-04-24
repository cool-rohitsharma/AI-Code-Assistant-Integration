package com.example.ai_service_integration.AiIntegration.controller;

import com.example.ai_service_integration.AiIntegration.dto.CompletionRequest;
import com.example.ai_service_integration.AiIntegration.dto.CompletionResponse;
import com.example.ai_service_integration.AiIntegration.service.AiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/complete")
    public Mono<ResponseEntity<CompletionResponse>> generateCompletion(@Valid @RequestBody CompletionRequest request) {
        logger.info("Received request for AI completion.");

        return aiService.getAiCompletion(request.getPrompt())
                .map(ResponseEntity::ok)
                .onErrorResume(RuntimeException.class, e -> {
                    logger.error("Error processing AI completion request", e);
                    // Return a bad request or internal server error depending on the error
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new CompletionResponse("Error generating completion: " + e.getMessage(), null)));
                });
    }
}
