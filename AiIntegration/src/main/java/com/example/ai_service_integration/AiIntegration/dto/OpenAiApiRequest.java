package com.example.ai_service_integration.AiIntegration.dto;;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OpenAiApiRequest {
    private String model;
    private List<Message> messages; // List of messages for chat completion
    private Integer max_tokens;
    private Double temperature;

    @Data
    @Builder
    public static class Message {
        private String role; // e.g., "system", "user", "assistant"
        private String content;
    }
}