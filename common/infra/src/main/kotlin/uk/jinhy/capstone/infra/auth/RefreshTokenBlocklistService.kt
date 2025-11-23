package uk.jinhy.capstone.infra.auth

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import java.time.Duration

interface RefreshTokenBlocklistService {
    fun addToBlocklist(token: String, expirationMillis: Long)
    fun isBlocked(token: String): Boolean
}

@Service
class RefreshTokenBlocklistServiceImpl(
    private val redissonClient: RedissonClient,
) : RefreshTokenBlocklistService {

    override fun addToBlocklist(token: String, expirationMillis: Long) {
        val bucket = redissonClient.getBucket<String>("refresh_token:blocklist:$token")
        bucket.set("blocked")
        bucket.expire(Duration.ofMillis(expirationMillis))
    }

    override fun isBlocked(token: String): Boolean {
        val bucket = redissonClient.getBucket<String>("refresh_token:blocklist:$token")
        return bucket.isExists
    }
}
