package com.school.common.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.school.common.multitenancy.TenantContext.HEADER_NAME;

@Configuration
public class OpenApiConfig {

    private static final String HEADER_PARAMETER_LOCATION = "header";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .version("1.0.0")
                        .description("User management microservice for School Management System"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT auth description")));
    }

    @Bean
    public OperationCustomizer schoolNameHeaderCustomizer(
            @Value("${app.tenant.default-schema:school_management}") String defaultSchema) {
        return (operation, handlerMethod) -> addSchoolNameHeader(operation, defaultSchema);
    }

    private Operation addSchoolNameHeader(Operation operation, String defaultSchema) {
        if (operation.getParameters() == null
                || operation.getParameters().stream()
                .noneMatch(parameter -> HEADER_NAME.equalsIgnoreCase(parameter.getName())
                        && HEADER_PARAMETER_LOCATION.equalsIgnoreCase(parameter.getIn()))) {
            operation.addParametersItem(new Parameter()
                    .in(HEADER_PARAMETER_LOCATION)
                    .name(HEADER_NAME)
                    .description("School schema used for this request")
                    .required(true)
                    .schema(new StringSchema()._default(defaultSchema)));
        }
        return operation;
    }
}
