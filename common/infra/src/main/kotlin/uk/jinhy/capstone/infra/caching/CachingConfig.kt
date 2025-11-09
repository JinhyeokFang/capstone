package uk.jinhy.capstone.infra.caching

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.util.StringUtils
import java.lang.reflect.Method
import java.time.Duration

@ConditionalOnProperty(prefix = "spring.data.redis", name = ["host"])
@Configuration
@EnableCaching
class CachingConfig(
    private val objectMapper: ObjectMapper,
    private val environment: Environment,
    private val cacheErrorHandler: CacheErrorHandler,
) : CachingConfigurer {

    override fun errorHandler(): CacheErrorHandler? {
        return cacheErrorHandler
    }

    @Bean
    fun defaultRedisCacheConfiguration(): RedisCacheConfiguration? {
        return RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .entryTtl(Duration.ofSeconds(60L))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    StringRedisSerializer(),
                ),
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(
                        objectMapper.copy().apply {
                            activateDefaultTyping(
                                objectMapper.polymorphicTypeValidator,
                                ObjectMapper.DefaultTyping.EVERYTHING,
                                JsonTypeInfo.As.PROPERTY,
                            )
                        },
                    ),
                ),
            )
    }

    @Bean
    override fun keyGenerator(): KeyGenerator {
        return ClassMethodParamKeyGenerator(environment)
    }
}

class ClassMethodParamKeyGenerator(
    private val environment: Environment,
) : KeyGenerator {

    override fun generate(target: Any, method: Method, vararg params: Any?): Any {
        val env = StringUtils.arrayToDelimitedString(environment.activeProfiles, ",")
        val classMethod = "${target.javaClass.simpleName}.${method.name}"
        val paramString = params.contentDeepToString()
        return "$env:$classMethod($paramString)"
    }
}
