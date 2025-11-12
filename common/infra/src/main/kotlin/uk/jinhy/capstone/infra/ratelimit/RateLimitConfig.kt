package uk.jinhy.capstone.infra.ratelimit

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
@ConditionalOnBean(RedisTemplate::class)
class RateLimitConfig {

    @Bean
    fun rateLimitUtil(redisTemplate: RedisTemplate<String, String>): RateLimitUtil {
        return RateLimitUtil(redisTemplate)
    }
}
