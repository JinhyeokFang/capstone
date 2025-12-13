package uk.jinhy.capstone.infra.sentry

import io.sentry.Sentry
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Profile("prod")
class SentryConfig : WebMvcConfigurer {
    private val logger = LoggerFactory.getLogger(SentryConfig::class.java)

    @PostConstruct
    fun init() {
        val sentryDsn = System.getenv("SENTRY_DSN")
        if (sentryDsn.isNullOrBlank()) {
            logger.info("SENTRY_DSN not found, Sentry tracking disabled")
            return
        }

        System.setProperty("sentry.dsn", sentryDsn)
    }

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
