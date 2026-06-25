package com.example.ProductComparison.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger 3.0) Configuration for Product Comparison Platform
 * 
 * This configuration class sets up the OpenAPI/Swagger documentation
 * for the Product Comparison API. It defines the API information,
 * security schemes (JWT authentication), and other metadata.
 * 
 * Swagger UI will be available at: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON will be available at: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI bean with API information and security schemes.
     * 
     * @return OpenAPI configuration object
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Comparison API")
                        .version("1.0.0")
                        .description("Comprehensive API documentation for the Product Comparison Platform. " +
                                "This API provides endpoints for user authentication, product management, " +
                                "product comparison, reviews, and specifications.")
                        .contact(new Contact()
                                .name("Product Comparison Team")
                                .email("support@productcomparison.com")
                                .url("https://productcomparison.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token for Bearer authentication. " +
                                                "Obtain this token from /auth/login or /auth/register endpoints.")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
