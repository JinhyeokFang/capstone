package uk.jinhy.capstone.api.auth.facade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.api.dto.response.LoginResponse
import uk.jinhy.capstone.api.auth.service.AuthService

class AuthFacadeTest : BehaviorSpec({

    val authService = mockk<AuthService>()
    val authFacade = AuthFacade(authService)

    Given("로그인 요청이 주어졌을 때") {
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123",
        )
        val expectedResponse = LoginResponse(accessToken = "test.jwt.token")

        every { authService.login(request) } returns expectedResponse

        When("로그인을 실행하면") {
            val response = authFacade.login(request)

            Then("AuthService의 login 메서드가 호출된다") {
                verify(exactly = 1) { authService.login(request) }
            }

            Then("올바른 응답이 반환된다") {
                response shouldBe expectedResponse
            }
        }
    }
})
