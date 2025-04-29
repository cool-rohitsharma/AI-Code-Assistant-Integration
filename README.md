Documentation: Spring Boot AI Service Integration Proof of Concept with JSON Web Token SecurityThis document presents a Proof of Concept (POC) project delineating the methodology for integrating an external Artificial Intelligence (AI) Application Programming Interface (API), specifically the OpenAI API utilizing the Chat Completions endpoint, into a Spring Boot service layer. The integration is fortified by the implementation of JSON Web Token (JWT) based authentication protocols.Salient FeaturesThe project encompasses the following key characteristics and functionalities:Adherence to the architectural structure of a Spring Boot application, version 3.2.x.Integration is established with the OpenAI Chat Completions API, accessible via the /v1/chat/completions endpoint.A dedicated service layer, comprising the AiService interface and its implementation OpenAiServiceImpl, is responsible for managing the logic pertaining to AI interaction.The WebClient component is employed for the execution of asynchronous, non-blocking Hypertext Transfer Protocol (HTTP) requests directed towards the external AI API.Configuration parameters, including API keys, Uniform Resource Locators (URLs), AI-specific settings (such as model selection, maximum token limits, and temperature), and JWT configuration, are managed through the application.yml file.A RESTful Controller, designated as AiController, is provided to expose an endpoint facilitating the initiation of AI completion processes.JWT Authentication: The /api/ai/** endpoints are subjected to security constraints.An endpoint, /authenticate, is designated for the procurement of a JWT upon successful validation of username and password credentials.The generation and validation of JWTs are handled through the utilization of the JJWT library.Spring Security is configured to enforce stateless JWT authentication.A custom implementation of UserDetailsService, operating on an in-memory basis for the purpose of this POC, is included for the loading of user details.Basic Data Transfer Objects (DTOs) are utilized for the handling of request and response payloads.Provisions for basic error handling and logging mechanisms are incorporated.PrerequisitesThe successful execution of this project necessitates the fulfillment of the following prerequisites:Installation of the Java Development Kit (JDK), version 17 or a more recent iteration.Availability of Apache Maven.Possession of an OpenAI API Key. Such a key may be obtained from the OpenAI platform (https://platform.openai.com/). It is pertinent to note that usage of this API is subject to associated costs.Procedural Steps for CommencementTo initiate the project, the following steps are to be undertaken:Project Establishment: A new Maven project is to be set up, and the requisite directory structure, as previously delineated, is to be created.Dependency Inclusion: The updated content of the pom.xml file is to be incorporated into the project's pom.xml, ensuring the inclusion of the spring-boot-starter-security and JJWT dependencies.Source File Creation: The Java classes and DTOs, as furnished in the preceding response, are to be created within their corresponding package structures (e.g., com.example.aiaiintegration.security, com.example.aiaiintegration.security.jwt, com.example.aiaiintegration.controller, com.example.aiaiintegration.dto). This includes the newly introduced security-related classes (JwtUtil, JwtRequestFilter, MyUserDetailsService, SecurityConfig, AuthController, AuthRequest, AuthResponse).Configuration File Generation: The src/main/resources/application.yml file is to be created.Configuration of OpenAI API Key and JWT Secret: The src/main/resources/application.yml file is to be accessed, and the placeholder YOUR_OPENAI_API_KEY is to be substituted with the actual OpenAI API key. Similarly, your_super_secret_jwt_key_replace_this_in_production is to be replaced with a robust, unique secret key designated for JWT signing.openai:
  api:
    url: https://api.openai.com/v1/chat/completions
    key: YOUR_OPENAI_API_KEY # !! Substitution with actual key is mandatory !!
    # ... other openai configs ...

jwt:
  secret: your_super_secret_jwt_key_replace_this_in_production # !! Substitution with a strong, unique secret is mandatory !!
  expiration: 86400000 # Token validity expressed in milliseconds (e.g., equivalent to 24 hours)

# ... other configs ...
CRITICAL ADVISORY: For production deployments, the storage of API keys or JWT secrets directly within application.yml is strictly contra-indicated. The utilization of environment variables, dedicated secrets management systems (such as Vault, AWS Secrets Manager, Azure Key Vault), or Spring Cloud Config in conjunction with encryption is recommended.Git Ignore File Creation: The .gitignore file is to be configured to prevent the inclusion of sensitive data within version control repositories.Building and Execution ProceduresThe process for building and executing the application is as follows:Access a terminal or command prompt positioned within the project's root directory.Initiate the project build process utilizing Apache Maven:mvn clean install
Execute the Spring Boot application:mvn spring-boot:run
Subsequent to execution, the application is expected to be operational and accessible at http://localhost:8080 (or the port specified within application.yml).Usage of API EndpointsThe API endpoints are to be utilized in the following manner:1. Procurement of JWT TokenEndpoint: POST /authenticateContent-Type: application/jsonRequest Body:{
  "username": "testuser",
  "password": "password123"
}
(Credentials correspond to the in-memory user defined within MyUserDetailsService)Success Response (HTTP Status 200 OK):{
  "jwt": "your_generated_jwt_token_here"
}
Error Response (HTTP Status 401 Unauthorized or other applicable error status):{
  "timestamp": "...",
  "status": 401,
  "error": "Unauthorized",
  "message": "Bad credentials", // Or other pertinent error message
  "path": "/authenticate"
}
Illustrative Example utilizing curl for token acquisition:curl -X POST \
  http://localhost:8080/authenticate \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "testuser",
    "password": "password123"
}'
2. Access to Secured AI EndpointFollowing the procurement of a JWT, said token is to be included within the Authorization header of subsequent requests directed towards the secured endpoint.Endpoint: POST /api/ai/completeContent-Type: application/jsonAuthorization Header: Bearer your_generated_jwt_token_hereRequest Body:{
  "prompt": "Your text prompt here"
}
Success Response (HTTP Status 200 OK):{
  "completion": "Generated text from AI",
  "model": "gpt-3.5-turbo-0125"
}
Error Response (HTTP Status 401 Unauthorized): This status is returned if the token is absent, invalid, or has expired.{
  "timestamp": "...",
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized", // Or other message provided by Spring Security
  "path": "/api/ai/complete"
}
Illustrative Example utilizing curl with JWT inclusion:The placeholder YOUR_JWT_TOKEN is to be replaced with the actual token obtained from the /authenticate endpoint.curl -X POST \
  http://localhost:8080/api/ai/complete \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -d '{
    "prompt": "Explain the concept of a microservice architecture in one paragraph."
}'
Overview of Code ComponentsThe project's codebase is structured as follows:AiAiIntegrationApplication.java: Represents the primary entry point for the Spring Boot application.config/OpenAiConfig.java: Contains the configuration for the WebClient component.dto/: This directory houses the DTOs utilized for API requests and responses (CompletionRequest, CompletionResponse, OpenAiApiRequest, OpenAiApiResponse), as well as those pertinent to Authentication (AuthRequest, AuthResponse).service/AiService.java, service/OpenAiServiceImpl.java: These components constitute the service layer responsible for AI interaction.controller/AiController.java: This is the secured REST controller designated for handling AI completion requests.controller/AuthController.java: This newly introduced REST controller is responsible for processing authentication requests and issuing JWTs.security/MyUserDetailsService.java: An implementation of UserDetailsService, operating in-memory, for the purpose of loading user details.security/SecurityConfig.java: The Spring Security configuration file, which defines the filter chain, authentication provider, password encoder, and session management policy.security/jwt/JwtUtil.java: A utility class dedicated to the generation, parsing, and validation of JWTs.security/jwt/JwtRequestFilter.java: A custom filter designed to intercept incoming requests, extract and validate JWTs, and subsequently configure the SecurityContext.application.yml: The configuration file encompassing both OpenAI and JWT-related settings.Considerations for Production DeploymentBeyond the considerations outlined in the preceding version, the following aspects warrant attention for a production-ready implementation:Database User Storage: The in-memory MyUserDetailsService should be superseded by an implementation that retrieves user details and their associated roles from a persistent database.Role-Based Authorization: The application of Spring Security's @PreAuthorize annotation or alternative method security mechanisms is recommended to restrict access to specific endpoints or data based on the roles assigned to users (e.g., .requestMatchers("/api/admin/**").hasRole("ADMIN")).Implementation of Refresh Tokens: The incorporation of refresh tokens is advisable to enhance user experience by mitigating the necessity for frequent re-authentication and to bolster security through the use of shorter-lived access tokens.Key Management: The secure management of the JWT signing key is paramount. Periodic rotation of keys is a recommended practice.Auditing: The logging of authentication attempts and failures is crucial for security monitoring.Rate Limiting: The /authenticate endpoint should be protected against brute-force attacks through the implementation of rate limiting mechanisms.Utilization of HTTPS: The exclusive use of HTTPS in a production environment is imperative to safeguard sensitive data, including credentials and JWTs, during transmission.LicenseThis project is made available under the terms of the MIT License.MIT License

Copyright (c) 2025 Yashika codelabs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
(The placeholders 2025 and Yashika Codelabs within the license text are to be populated with the relevant information).
