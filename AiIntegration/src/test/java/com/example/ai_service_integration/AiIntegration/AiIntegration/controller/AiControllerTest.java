package com.example.ai_service_integration.AiIntegration.AiIntegration.controller;

import com.example.ai_service_integration.AiIntegration.controller.AiController;
import com.example.ai_service_integration.AiIntegration.dto.CompletionRequest;
import com.example.ai_service_integration.AiIntegration.dto.CompletionResponse;
import com.example.ai_service_integration.AiIntegration.service.AiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock // Mock the dependency AiService
    private AiService aiService;

    @InjectMocks // Inject mocks into the controller instance
    private AiController aiController;

    @Test
    void generateCompletion_Success() {
        // Arrange: Define the expected service response
        String prompt = "Test prompt";
        String generatedCompletion = "Generated text";
        String modelUsed = "test-model";
        CompletionResponse serviceResponse = new CompletionResponse(generatedCompletion, modelUsed);

        // Mock the service call to return a successful Mono
        when(aiService.getAiCompletion(prompt))
                .thenReturn(Mono.just(serviceResponse));

        CompletionRequest request = new CompletionRequest(prompt);

        // Act & Assert: Test the controller method's Mono output
        StepVerifier.create(aiController.generateCompletion(request))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode().equals(HttpStatus.OK) &&
                                responseEntity.getBody() != null &&
                                responseEntity.getBody().getCompletion().equals(generatedCompletion) &&
                                responseEntity.getBody().getModel().equals(modelUsed)
                )
                .verifyComplete();
    }

    @Test
    void generateCompletion_ServiceReturnsError() {
        // Arrange: Define the error from the service
        String prompt = "Test prompt that causes error";
        RuntimeException serviceError = new RuntimeException("Service failed to get completion");

        // Mock the service call to return an error Mono
        when(aiService.getAiCompletion(prompt))
                .thenReturn(Mono.error(serviceError));

        CompletionRequest request = new CompletionRequest(prompt);

        // Act & Assert
        StepVerifier.create(aiController.generateCompletion(request))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR) &&
                                responseEntity.getBody() != null &&
                                responseEntity.getBody().getCompletion().contains("Error generating completion") &&
                                responseEntity.getBody().getCompletion().contains("Service failed to get completion") &&
                                responseEntity.getBody().getModel() == null // Model might be null on error
                )
                .verifyComplete(); // Expect it to complete with the error response entity
    }

    // Note: Testing @Valid (like @NotBlank) is typically done with Spring's
    // @WebMvcTest or a combination of Spring test context and MockMvc,
    // as the validation process happens before the controller method body
    // is executed in a typical web request flow. Pure Mockito unit test
    // doesn't easily simulate this pre-method validation.

}