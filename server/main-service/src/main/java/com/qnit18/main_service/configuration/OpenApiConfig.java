package com.qnit18.main_service.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI mainServiceOpenAPI() {
        Server server = new Server();
        String serverUrl = contextPath.isEmpty() ? "/" : contextPath;
        server.setUrl(serverUrl);
        server.setDescription("Main Service Server");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Main Service API")
                        .description("REST API for GenZF Main Service - Asset Management, Portfolio, Budget Rules, and Chart Data")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("GenZF Team")
                                .email("support@genzf.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

