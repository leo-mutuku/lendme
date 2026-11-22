package group.lendme.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    /** Paths Swagger UI / OpenAPI need; permit these in the SecurityFilterChain. */
    public static final String[] SWAGGER_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public OpenAPI lendmeOpenApi(@Value("${spring.application.name}") String appName) {
        return new OpenAPI()
                .info(new Info()
                        .title(appName + " API")
                        .description("REST API for the LendMe lending platform")
                        .version("v1"))
                // Applies the bearer token to every endpoint by default.
                // Use @SecurityRequirements() on public endpoints (e.g. login) to opt out.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
