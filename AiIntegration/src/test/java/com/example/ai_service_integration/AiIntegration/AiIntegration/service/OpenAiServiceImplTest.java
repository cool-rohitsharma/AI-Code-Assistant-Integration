package com.example.ai_service_integration.AiIntegration.AiIntegration.service;


import com.example.ai_service_integration.AiIntegration.dto.OpenAiApiRequest;
import com.example.ai_service_integration.AiIntegration.dto.OpenAiApiResponse;
import com.example.ai_service_integration.AiIntegration.service.OpenAiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class) // Use JUnit 5 extension for Mockito
class OpenAiServiceImplTest {

    @Mock // Mock the dependency WebClient
    private WebClient webClient;

    @Mock // Mock the chain of WebClient calls
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RequestBodySpec requestBodySpec;
    @Mock
    private RequestHeadersSpec requestHeadersSpec;
    @Mock
    private ResponseSpec responseSpec;

    @InjectMocks // Inject mocks into the service instance
    private OpenAiServiceImpl openAiService;

    // Values that would normally be injected by @Value
    private String model = "gpt-test-model";
    private Integer maxTokens = 100;
    private Double temperature = 0.5;

    @BeforeEach
    void setUp() {
        // Manually inject the @Value fields into the service instance for unit tests
        ReflectionTestUtils.setField(openAiService, "model", model);
        ReflectionTestUtils.setField(openAiService, "maxTokens", maxTokens);
        ReflectionTestUtils.setField(openAiService, "temperature", temperature);

        // --- Corrected Mocking Chain ---
        when(webClient.post()).thenReturn(requestBodyUriSpec); // webClient.post() returns requestBodyUriSpec

        // Service calls .contentType() on the result of .post() (requestBodyUriSpec)
        when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodySpec); // CORRECT: Stubbing contentType on requestBodyUriSpec

        // Service calls .bodyValue() on the result of .contentType() (requestBodySpec)
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);

        // Service calls .retrieve() on the result of .bodyValue() (requestHeadersSpec)
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // --- End Corrected Mocking Chain ---

        // Mocking uri(anyString()) is not strictly needed based on the service code provided,
        // as the chain is .post().contentType().bodyValue().retrieve()...
        // If the service code changed to use .uri() after .post(), you would uncomment/add this:
        // when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    }

    @Test
    void getAiCompletion_Success() {
        // Arrange: Mock the successful API response structure
        String expectedCompletionContent = "This is a test completion.";
        OpenAiApiResponse mockApiResponse = new OpenAiApiResponse();
        OpenAiApiResponse.Choice mockChoice = new OpenAiApiResponse.Choice();
        OpenAiApiRequest.Message mockMessage = OpenAiApiRequest.Message.builder().role("assistant").content(expectedCompletionContent).build();
        mockChoice.setMessage(mockMessage);
        mockChoice.setIndex(0);
        mockApiResponse.setChoices(Collections.singletonList(mockChoice));
        mockApiResponse.setModel(model);
        mockApiResponse.setId("test-id");
        mockApiResponse.setObject("chat.completion");
        mockApiResponse.setCreated(System.currentTimeMillis() / 1000);
        mockApiResponse.setUsage(new OpenAiApiResponse.Usage(10, 20, 30));


        when(responseSpec.bodyToMono(OpenAiApiResponse.class))
                .thenReturn(Mono.just(mockApiResponse));

        String prompt = "Test prompt";

        // Act & Assert: Use StepVerifier to test the Mono flow
        StepVerifier.create(openAiService.getAiCompletion(prompt))
                .expectNextMatches(response ->
                        response.getCompletion().equals(expectedCompletionContent) &&
                                response.getModel().equals(model)
                )
                .verifyComplete();
    }

    @Test
    void getAiCompletion_SuccessWithLeadingTrailingWhitespaceInCompletion() {
        // Arrange: Mock the successful API response structure with whitespace
        String rawCompletionContent = " \n This is a test completion with whitespace. \t";
        String expectedCompletionContent = "This is a test completion with whitespace."; // Trimmed
        OpenAiApiResponse mockApiResponse = new OpenAiApiResponse();
        OpenAiApiResponse.Choice mockChoice = new OpenAiApiResponse.Choice();
        OpenAiApiRequest.Message mockMessage = OpenAiApiRequest.Message.builder().role("assistant").content(rawCompletionContent).build();
        mockChoice.setMessage(mockMessage);
        mockApiResponse.setChoices(Collections.singletonList(mockChoice));
        mockApiResponse.setModel(model);
        // ... set other minimal fields for OpenAiApiResponse if needed by the code path

        when(responseSpec.bodyToMono(OpenAiApiResponse.class))
                .thenReturn(Mono.just(mockApiResponse));

        String prompt = "Test prompt";

        // Act & Assert: Use StepVerifier to test the Mono flow
        StepVerifier.create(openAiService.getAiCompletion(prompt))
                .expectNextMatches(response ->
                        response.getCompletion().equals(expectedCompletionContent) &&
                                response.getModel().equals(model)
                )
                .verifyComplete();
    }


    @Test
    void getAiCompletion_ApiReturnsEmptyChoices() {
        // Arrange: Mock an API response with empty choices list
        OpenAiApiResponse mockApiResponse = new OpenAiApiResponse();
        mockApiResponse.setChoices(Collections.emptyList()); // Empty list
        mockApiResponse.setModel(model);
        // ... set other minimal fields

        when(responseSpec.bodyToMono(OpenAiApiResponse.class))
                .thenReturn(Mono.just(mockApiResponse));

        String prompt = "Test prompt";
        String expectedMessage = "No completion generated.";

        // Act & Assert
        StepVerifier.create(openAiService.getAiCompletion(prompt))
                .expectNextMatches(response ->
                        response.getCompletion().equals(expectedMessage) &&
                                response.getModel().equals(model) // Still return the model if available
                )
                .verifyComplete();
    }

    @Test
    void getAiCompletion_ApiReturnsChoicesWithNullMessage() {
        // Arrange: Mock an API response with choices list containing a null message
        OpenAiApiResponse mockApiResponse = new OpenAiApiResponse();
        OpenAiApiResponse.Choice mockChoice = new OpenAiApiResponse.Choice();
        mockChoice.setMessage(null); // Null message
        mockApiResponse.setChoices(Collections.singletonList(mockChoice));
        mockApiResponse.setModel(model);
        // ... set other minimal fields

        when(responseSpec.bodyToMono(OpenAiApiResponse.class))
                .thenReturn(Mono.just(mockApiResponse));

        String prompt = "Test prompt";
        String expectedMessage = "No completion generated.";

        // Act & Assert
        StepVerifier.create(openAiService.getAiCompletion(prompt))
                .expectNextMatches(response ->
                        response.getCompletion().equals(expectedMessage) &&
                                response.getModel().equals(model)
                )
                .verifyComplete();
    }


    @Test
    void getAiCompletion_ApiReturnsWebClientResponseException() {
        // Arrange: Mock an API error response (e.g., 401 Unauthorized, 400 Bad Request)
        WebClientResponseException mockException = new WebClientResponseException(
                BAD_REQUEST.value(), // Status code
                "Bad Request",       // Status text
                null,                // Headers
                "{\"error\": {\"message\": \"Invalid request format.\"}}".getBytes(), // Response body
                null,                // Charset
                null                 // Request
        );

        when(responseSpec.bodyToMono(OpenAiApiResponse.class))
                .thenReturn(Mono.error(mockException)); // Return an error Mono

        String prompt = "Invalid test prompt";

        // Act & Assert
        StepVerifier.create(openAiService.getAiCompletion(prompt))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException && // Expect the wrapped RuntimeException
                                throwable.getMessage().contains("Error calling OpenAI API") &&
                                throwable.getMessage().contains("Invalid request format.") // Check for message from API body
                )
                .verify(); // Use verify() for error scenarios
    }

    @Test
    void getAiCompletion_ApiReturnsChoiceWithEmptyContent() {
        // Arrange: Mock an API response with a choice containing empty content
        OpenAiApiResponse mockApiResponse = new OpenAiApiResponse();
        OpenAiApiResponse.Choice mockChoice = new OpenAiApiResponse.Choice();
        OpenAiApiRequest.Message mockMessage = OpenAiApiRequest.Message.builder().role("assistant").content("").build(); // Empty content
        mockChoice.setMessage(mockMessage);
        mockApiResponse.setChoices(Collections.singletonList(mockChoice));
        mockApiResponse.setModel(model);
        // ... set other minimal fields

        when(responseSpec.bodyToMono(OpenAiApiResponse.class))
                .thenReturn(Mono.just(mockApiResponse));

        String prompt = "Test prompt";
        String expectedMessage = "No completion generated.";

        // Act & Assert
        StepVerifier.create(openAiService.getAiCompletion(prompt))
                .expectNextMatches(response ->
                        response.getCompletion().equals(expectedMessage) &&
                                response.getModel().equals(model)
                )
                .verifyComplete();
    }

    @Test
    void getAiCompletion_ApiReturnsChoiceWithBlankContent() {
        // Arrange: Mock an API response with a choice containing only whitespace content
        OpenAiApiResponse mockApiResponse = new OpenAiApiResponse();
        OpenAiApiResponse.Choice mockChoice = new OpenAiApiResponse.Choice();
        OpenAiApiRequest.Message mockMessage = OpenAiApiRequest.Message.builder().role("assistant").content("   \n ").build(); // Blank content
        mockChoice.setMessage(mockMessage);
        mockApiResponse.setChoices(Collections.singletonList(mockChoice));
        mockApiResponse.setModel(model);
        // ... set other minimal fields

        when(responseSpec.bodyToMono(OpenAiApiResponse.class))
                .thenReturn(Mono.just(mockApiResponse));

        String prompt = "Test prompt";
        String expectedMessage = "No completion generated.";

        // Act & Assert
        StepVerifier.create(openAiService.getAiCompletion(prompt))
                .expectNextMatches(response ->
                        response.getCompletion().equals(expectedMessage) &&
                                response.getModel().equals(model)
                )
                .verifyComplete();
    }
}