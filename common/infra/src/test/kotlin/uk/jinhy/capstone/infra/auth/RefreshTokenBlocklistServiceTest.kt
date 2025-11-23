package uk.jinhy.capstone.infra.auth

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.redisson.api.RBucket
import org.redisson.api.RedissonClient
import java.time.Duration

class RefreshTokenBlocklistServiceTest : BehaviorSpec({

    lateinit var redissonClient: RedissonClient
    lateinit var refreshTokenBlocklistService: RefreshTokenBlocklistService

    beforeEach {
        redissonClient = mockk()
        refreshTokenBlocklistService = RefreshTokenBlocklistServiceImpl(redissonClient)
    }

    given("토큰을 블록리스트에 추가") {
        `when`("유효한 토큰과 만료 시간이 주어지면") {
            val token = "test-refresh-token"
            val expirationMillis = 3600000L

            val bucket = mockk<RBucket<String>>(relaxed = true)

            every { redissonClient.getBucket<String>("refresh_token:blocklist:$token") } returns bucket
            every { bucket.set("blocked") } returns Unit
            every { bucket.expire(Duration.ofMillis(expirationMillis)) } returns true

            then("토큰을 블록리스트에 추가하고 만료 시간을 설정한다") {
                refreshTokenBlocklistService.addToBlocklist(token, expirationMillis)

                verify(exactly = 1) { redissonClient.getBucket<String>("refresh_token:blocklist:$token") }
                verify(exactly = 1) { bucket.set("blocked") }
                verify(exactly = 1) { bucket.expire(Duration.ofMillis(expirationMillis)) }
            }
        }
    }

    given("토큰이 블록리스트에 있는지 확인") {
        `when`("블록리스트에 있는 토큰을 확인하면") {
            val token = "blocked-token"

            val bucket = mockk<RBucket<String>>()

            every { redissonClient.getBucket<String>("refresh_token:blocklist:$token") } returns bucket
            every { bucket.isExists } returns true

            then("true를 반환한다") {
                val result = refreshTokenBlocklistService.isBlocked(token)

                result shouldBe true

                verify(exactly = 1) { redissonClient.getBucket<String>("refresh_token:blocklist:$token") }
                verify(exactly = 1) { bucket.isExists }
            }
        }

        `when`("블록리스트에 없는 토큰을 확인하면") {
            val token = "valid-token"

            val bucket = mockk<RBucket<String>>()

            every { redissonClient.getBucket<String>("refresh_token:blocklist:$token") } returns bucket
            every { bucket.isExists } returns false

            then("false를 반환한다") {
                val result = refreshTokenBlocklistService.isBlocked(token)

                result shouldBe false

                verify(exactly = 1) { redissonClient.getBucket<String>("refresh_token:blocklist:$token") }
                verify(exactly = 1) { bucket.isExists }
            }
        }
    }
})
