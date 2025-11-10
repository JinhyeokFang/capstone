package uk.jinhy.capstone.infra.feign

import feign.Logger
import feign.Request
import feign.Retryer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class FeignConfig {
    @Bean
    fun feignRetryer(): Retryer {
        return Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3)
    }

    @Bean
    fun feignOptions(): Request.Options {
        val connectTimeoutMillis: Long = 5000
        val readTimeoutMillis: Long = 10000
        val followRedirects = true
        return Request.Options(
            connectTimeoutMillis,
            TimeUnit.MILLISECONDS,
            readTimeoutMillis,
            TimeUnit.MILLISECONDS,
            followRedirects,
        )
    }

    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.FULL
    }
}
