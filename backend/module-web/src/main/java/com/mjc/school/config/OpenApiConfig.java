package com.mjc.school.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("news-api")
                .packagesToScan("mjc.newsApplication.controller")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI newsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FwaltNews Rest API")
                        .description("News Application API")
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("Foko_Walter")
                                .url("fwaltnews.com")
                                .email("fokowalter17@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
