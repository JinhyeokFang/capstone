package uk.jinhy.capstone.api.auth.repository

import io.kotest.matchers.shouldBe
import uk.jinhy.capstone.api.support.IntegrationTestSupport

class RefreshTokenBlocklistServiceTest : IntegrationTestSupport() {

    init {
        Given("리프레시 토큰이 주어졌을 때") {
            val token = "test.refresh.token"
            val expirationMillis = 604800000L

            When("토큰을 블록리스트에 추가하면") {
                refreshTokenBlocklistService.addToBlocklist(token, expirationMillis)

                Then("토큰이 블록리스트에 있어야 한다") {
                    val isBlocked = refreshTokenBlocklistService.isBlocked(token)
                    isBlocked shouldBe true
                }
            }
        }

        Given("블록리스트에 없는 토큰이 주어졌을 때") {
            val token = "not.in.blocklist.token"

            When("토큰이 블록리스트에 있는지 확인하면") {
                Then("false를 반환한다") {
                    val isBlocked = refreshTokenBlocklistService.isBlocked(token)
                    isBlocked shouldBe false
                }
            }
        }

        Given("블록리스트에 여러 토큰이 추가되었을 때") {
            val token1 = "token1"
            val token2 = "token2"
            val token3 = "token3"
            val expirationMillis = 604800000L

            When("일부 토큰만 블록리스트에 추가하면") {
                refreshTokenBlocklistService.addToBlocklist(token1, expirationMillis)
                refreshTokenBlocklistService.addToBlocklist(token2, expirationMillis)

                Then("추가된 토큰만 블록리스트에 있어야 한다") {
                    refreshTokenBlocklistService.isBlocked(token1) shouldBe true
                    refreshTokenBlocklistService.isBlocked(token2) shouldBe true
                    refreshTokenBlocklistService.isBlocked(token3) shouldBe false
                }
            }
        }
    }
}
