package com.khantech.assignment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI openApi() {
        Server server = new Server();
        server.setUrl("/");
        server.setDescription("API");
        return new OpenAPI()
                .servers(List.of(server));
    }
}