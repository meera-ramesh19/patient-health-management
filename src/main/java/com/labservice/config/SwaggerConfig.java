package com.labservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ─────────────────────────────────────────────────────────────
// SWAGGER / OPENAPI CONFIGURATION
// ─────────────────────────────────────────────────────────────
// Swagger auto-generates a web page that documents your entire API.
// Visit: http://localhost:8080/swagger-ui.html
//
// What you get:
//   - Every endpoint listed with its HTTP method, URL, and parameters
//   - A "Try it out" button to test each endpoint right in the browser
//   - Request/response body examples
//   - An "Authorize" button to paste your JWT token
//
// This class customizes what appears on that page:
//   - App name, description, version
//   - JWT authentication setup (so you can test protected endpoints)
//
// Without this class: Swagger still works, but with generic defaults
// and no JWT auth button.
// ─────────────────────────────────────────────────────────────

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI labServiceOpenAPI() {
        return new OpenAPI()
                // ── App Info (shown at the top of the Swagger page) ──
                .info(new Info()
                        .title("LabService API")
                        .description("Healthcare management REST API with Patient, Doctor, and Admin portals. "
                                + "Use the Authorize button with a JWT token to test protected endpoints.")
                        .version("1.0.0"))

                // ── JWT Authentication ──
                // Adds an "Authorize" button to the Swagger UI.
                // Click it, paste your JWT token, and all subsequent requests
                // will include the Authorization: Bearer <token> header.
                //
                // Without this: you can see all endpoints but can't test
                // the ones that require login (everything except /api/auth/**)
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Bearer Authentication")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token (without 'Bearer ' prefix)")));
    }
}
