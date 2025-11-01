package uk.jinhy.capstone.api.config.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.In
import io.swagger.v3.oas.models.security.SecurityScheme.Type
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .addSecurityItem(SecurityRequirement().addList("authorization"))
            .components(
                Components().addSecuritySchemes(
                    "authorization",
                    SecurityScheme()
                        .type(Type.APIKEY)
                        .`in`(In.HEADER)
                        .name("authorization"),
                ),
            )
            .info(apiInfo())
            .servers(
                listOf(
                    Server().url("/"),
                ),
            )
    }

    private fun apiInfo(): Info {
        return Info()
            .title("Capstone API")
            .description("Capstone API")
            .version("1.0.0")
    }
}
