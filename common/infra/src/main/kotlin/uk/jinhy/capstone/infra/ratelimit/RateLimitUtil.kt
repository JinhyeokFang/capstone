package uk.jinhy.capstone.infra.ratelimit

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.time.Duration

class RateLimitUtil(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    companion object {
        private val LUA_SCRIPT = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local current = redis.call('INCR', key)
            if current == 1 then
                redis.call('EXPIRE', key, window)
            end
            return {current, limit}
        """.trimIndent()

        private val SCRIPT: DefaultRedisScript<List<*>> = DefaultRedisScript(LUA_SCRIPT, List::class.java)
    }

    fun isAllowed(
        key: String,
        maxRequests: Long,
        windowSeconds: Long,
    ): Boolean {
        val result = redisTemplate.execute(
            SCRIPT,
            listOf(key),
            maxRequests.toString(),
            windowSeconds.toString(),
        ) ?: return false

        if (result.size < 2) return false
        val current = (result[0] as? Number)?.toLong() ?: return false
        val limit = (result[1] as? Number)?.toLong() ?: return false
        return current <= limit
    }

    fun tryConsume(
        key: String,
        maxRequests: Long,
        windowSeconds: Long,
    ): RateLimitResult {
        val result = redisTemplate.execute(
            SCRIPT,
            listOf(key),
            maxRequests.toString(),
            windowSeconds.toString(),
        ) ?: return RateLimitResult(false, 0, maxRequests, Duration.ofSeconds(windowSeconds))

        if (result.size < 2) {
            return RateLimitResult(false, 0, maxRequests, Duration.ofSeconds(windowSeconds))
        }

        val current = (result[0] as? Number)?.toLong() ?: 0L
        val limit = (result[1] as? Number)?.toLong() ?: maxRequests
        val remaining = maxOf(0, limit - current)
        val resetAfter = Duration.ofSeconds(windowSeconds)

        return RateLimitResult(
            allowed = current <= limit,
            remaining = remaining,
            limit = limit,
            resetAfter = resetAfter,
        )
    }

    fun reset(key: String) {
        redisTemplate.delete(key)
    }
}

data class RateLimitResult(
    val allowed: Boolean,
    val remaining: Long,
    val limit: Long,
    val resetAfter: Duration,
)
