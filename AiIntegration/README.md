# Spring Boot AI Service Integration POC

This project is a Proof of Concept (POC) demonstrating how to integrate an external Artificial Intelligence (AI) API (specifically, the OpenAI API using the Chat Completions endpoint) into a Spring Boot service layer. The goal is to showcase how an application can leverage AI capabilities at runtime.

**Note on "Logic Scaffolding":** While the initial request mentioned "real-time logic scaffolding," this POC focuses on integrating AI capabilities *within* the application's runtime logic (e.g., generating text based on user input). Traditional AI code assistants like Copilot are developer tools used *during coding* to help *write* the application's code, which is a different context from integrating AI into the deployed service layer for runtime tasks. This project demonstrates the latter.

## Features

* Spring Boot 3.2.x application structure.
* Integration with the OpenAI Chat Completions API (`/v1/chat/completions`).
* Service layer (`AiService`, `OpenAiServiceImpl`) for handling AI interaction logic.
* `WebClient` for making asynchronous, non-blocking HTTP calls to the OpenAI API.
* Configuration using `application.yml` for API keys, URLs, and AI parameters (model, max tokens, temperature).
* REST Controller (`AiController`) to expose an endpoint for triggering AI completion.
* Basic DTOs for request and response handling.
* Basic error handling for API calls.
* Basic logging.

## Prerequisites

* Java Development Kit (JDK) 17 or higher
* Apache Maven
* An OpenAI API Key (You can get one from [https://platform.openai.com/](https://platform.openai.com/). Note that usage incurs costs.)

## Getting Started

1.  **Clone or Create Project:** Set up a new Maven project and create the directory structure as described in the previous response.
2.  **Add Dependencies:** Copy the `pom.xml` content into your `pom.xml` file.
3.  **Create Source Files:** Create the Java classes and DTOs as provided in the previous response within the `src/main/java` directory under the appropriate package structure (`com.example.aiaiintegration`).
4.  **Create Configuration File:** Create `src/main/resources/application.yml`.
5.  **Configure OpenAI API Key:** Open `src/main/resources/application.yml` and replace `YOUR_OPENAI_API_KEY` with your actual OpenAI API key.
    ```yaml
    openai:
      api:
        url: [https://api.openai.com/v1/chat/completions](https://api.openai.com/v1/chat/completions)
        key: YOUR_OPENAI_API_KEY # !! REPLACE THIS WITH YOUR ACTUAL KEY !!
        model: gpt-3.5-turbo # Or another suitable model
        timeout: 10000
        max-tokens: 500
        temperature: 0.7
    # ... other configurations ...
    ```
    **IMPORTANT:** For production deployments, **DO NOT** store API keys directly in `application.yml`. Use environment variables, secrets management systems (like Vault, AWS Secrets Manager, Azure Key Vault), or Spring Cloud Config with encryption.
6.  **Create .gitignore:** Add the `.gitignore` file to prevent sensitive data (like the API key if you forgot to remove it from application.yml before committing, or compiled classes) from being committed to version control.

## Building and Running

1.  Open a terminal or command prompt in the project's root directory (`ai-service-integration`).
2.  Build the project using Maven:
    ```bash
    mvn clean install
    ```
3.  Run the Spring Boot application:
    ```bash
    mvn spring-boot:run
    ```
    The application should start and be accessible at `http://localhost:8080` (or the port configured in `application.yml`).

## API Endpoint Usage

The application exposes a single POST endpoint for generating AI completions.

* **Endpoint:** `POST /api/ai/complete`
* **Content-Type:** `application/json`
* **Request Body:**
    ```json
    {
      "prompt": "Your text prompt here"
    }
    ```
  The `prompt` field is required and cannot be empty.
* **Success Response (200 OK):**
    ```json
    {
      "completion": "Generated text from AI",
      "model": "gpt-3.5-turbo-0125"
    }
    ```
* **Error Response (500 Internal Server Error or other HTTP status):**
    ```json
    {
      "completion": "Error message details",
      "model": null
    }
    ```

**Example using `curl`:**

```bash
curl -X POST \
  http://localhost:8080/api/ai/complete \
  -H 'Content-Type: application/json' \
  -d '{
    "prompt": "Explain the concept of a microservice architecture in one paragraph."
}'