package backend.app.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI knowledgeSearchOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Search API")
                        .version("1.0.0")
                        .description("Backend API for document upload, indexing, full-text search and query history")
                        .license(new License().name("")));
    }
}
