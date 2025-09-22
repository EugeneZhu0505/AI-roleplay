package com.hzau.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: OpenApiConfig
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/22 下午7:17
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AI角色扮演系统API",
                version = "1.0.0",
                description = "AI角色扮演网站后端API接口文档"
        )
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT认证令牌")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}

