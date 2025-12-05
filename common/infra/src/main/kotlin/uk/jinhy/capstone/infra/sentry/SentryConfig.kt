package uk.jinhy.capstone.infra.sentry

import io.sentry.Sentry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Profile("prod")
class SentryConfig : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SentryContextInterceptor())
    }

    class SentryContextInterceptor : HandlerInterceptor {
        override fun preHandle(
            request: HttpServletRequest,
            response: HttpServletResponse,
            handler: Any,
        ): Boolean {
            Sentry.configureScope { scope ->
                scope.setTag("http.method", request.method)
                scope.setTag("http.url", request.requestURI)
                scope.setTag("http.query_string", request.queryString ?: "")
                scope.setContexts(
                    "request",
                    mapOf(
                        "method" to request.method,
                        "url" to request.requestURI,
                        "query_string" to (request.queryString ?: ""),
                        "remote_addr" to (request.remoteAddr ?: ""),
                    ),
                )
            }
            return true
        }
    }
}
