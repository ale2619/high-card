package it.sara.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI 3.1 specification exposed at {@code /v3/api-docs}
 * and the Swagger UI available at {@code /swagger-ui.html}.
 * <p>
 * A global Bearer JWT security scheme is registered so that every protected
 * endpoint shows the padlock icon in the UI and can be tested directly after
 * pasting the token obtained from {@code POST /auth/login}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HighCard API")
                        .version("0.0.1")
                        .description("Spring Boot REST API — user management with JWT authentication."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained via POST /auth/login. Paste the value of the 'token' field.")));
    }
}