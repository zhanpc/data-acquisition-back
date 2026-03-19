package com.maplestone.dataCollect.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Swagger3Config {

        @Bean
        public OpenAPI springShopOpenApi() {
                final String loginToken = "BearerAuth";
                return new OpenAPI().info(new Info().title("Simple Boot API")
                                .description("SpringBoot基础框架")
                                .version("v1.0.0")).externalDocs(new ExternalDocumentation()
                                                .description("SpringBoot基础框架")
                                                .url("http://localhost:8080"))
                                .components(new Components().addSecuritySchemes(loginToken,
                                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                                                .scheme("bearer").bearerFormat("JWT")
                                                                .in(SecurityScheme.In.HEADER)
                                                                .name(loginToken)))
                                .addSecurityItem(new SecurityRequirement().addList(loginToken));
        }

}
