package com.example.ai_service_integration.AiIntegration.service;

import com.example.ai_service_integration.AiIntegration.dto.OpenAiApiRequest;
import com.example.ai_service_integration.AiIntegration.dto.CompletionResponse;
import com.example.ai_service_integration.AiIntegration.dto.OpenAiApiRequest.Message;
import com.example.ai_service_integration.AiIntegration.dto.OpenAiApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
public class OpenAiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiServiceImpl.class);

    private final WebClient openAiWebClient;

    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.max-tokens}")
    private Integer maxTokens;

    @Value("${openai.api.temperature}")
    private Double temperature;

    public OpenAiServiceImpl(@Qualifier("openAiWebClient") WebClient openAiWebClient) {
        this.openAiWebClient = openAiWebClient;
    }

    @Override
    public Mono<CompletionResponse> getAiCompletion(String prompt) {
        logger.debug("Sending prompt to OpenAI: {}", prompt);

        OpenAiApiRequest requestBody = OpenAiApiRequest.builder()
                .model(model)
                .messages(Collections.singletonList(
                        Message.builder().role("user").content(prompt).build()
                ))
                .max_tokens(maxTokens)
                .temperature(temperature)
                .build();

        return openAiWebClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAiApiResponse.class)
                .map(apiResponse -> {
                    String completion = apiResponse.getFirstCompletionContent();
                    if (completion == null || completion.trim().isEmpty()) {
                        logger.warn("OpenAI API returned empty or null completion for prompt: {}", prompt);
                        return new CompletionResponse("No completion generated.", apiResponse.getModel());
                    }
                    logger.debug("Received completion from OpenAI.");
                    return new CompletionResponse(completion.trim(), apiResponse.getModel());
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("OpenAI API error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
                    return Mono.error(new RuntimeException("Error calling OpenAI API: " + e.getResponseBodyAsString()));
                })
                .onErrorResume(e -> {
                    logger.error("Generic error calling OpenAI API", e);
                    return Mono.error(new RuntimeException("An unexpected error occurred while calling OpenAI API.", e));
                });
    }
}