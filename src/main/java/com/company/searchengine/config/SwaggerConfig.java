package com.company.searchengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import java.util.ArrayList;
import java.util.List;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${application.swagger.custom-headers}")
    private List<String> customHeaders;

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(
                new Info().title("ms-search-engine")
                        .description("API for ms-search-engine service")
                        .version("1.0.0")
        );
    }

    @Bean
    public GlobalOpenApiCustomizer globalHeaderCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().stream()
                        .flatMap(pathItem -> pathItem.readOperations().stream())
                        .forEach(this::addHeaderParameters);
            }
        };
    }

    private void addHeaderParameters(Operation operation) {
        customHeaders.forEach(header -> {
            if (operation.getParameters() == null) {
                operation.setParameters(new ArrayList<>());
            }
            if (checkIfHeaderAdded(operation, header)) {
                operation.addParametersItem(new HeaderParameter().name(header));
            }
        });
    }

    private boolean checkIfHeaderAdded(Operation operation, String header) {
        return operation.getParameters().stream()
                .noneMatch(parameter -> parameter.getName().equals(header));
    }

}
