package uk.jinhy.capstone.api.auth.presentation

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthLoginResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthMeResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthRefreshResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthSignUpResponseDto
import uk.jinhy.capstone.api.auth.facade.AuthFacade
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.util.jwt.JwtUtil
import java.time.Instant

class AuthControllerTest : BehaviorSpec({
    val authFacade = mockk<AuthFacade>()
    val authController = AuthController(authFacade)

    beforeSpec {
        JwtUtil.initialize(
            secretKey = "test-secret-key-for-testing-purposes-only-minimum-256-bits-required-for-hmac-sha-algorithm",
            accessTokenExpirationMillis = 600000,
            refreshTokenExpirationMillis = 604800000,
        )
    }

    Given("로그인 요청이 있을 때") {
        val request = AuthLoginRequestDto(
            email = "test@example.com",
            password = "password123",
        )
        val loginResponse = AuthLoginResponseDto(accessToken = "access-token")
        val refreshToken = "refresh-token"

        When("인증이 성공하면") {
            val response = mockk<HttpServletResponse>(relaxed = true)
            every { authFacade.login(request) } returns Pair(loginResponse, refreshToken)
            every { response.setHeader(any(), any()) } returns Unit

            val result = authController.login(request, response)

            Then("ApiResponse를 반환하고 쿠키를 설정한다") {
                result.success shouldBe true
                result.data?.accessToken shouldBe "access-token"
                verify(exactly = 1) { authFacade.login(request) }
                verify(exactly = 1) { response.setHeader("Set-Cookie", any()) }
            }
        }
    }

    Given("회원가입 요청이 있을 때") {
        val request = AuthSignUpRequestDto(
            name = "Test User",
            email = "test@example.com",
            password = "password123",
        )
        val signUpResponse = AuthSignUpResponseDto(accessToken = "access-token")
        val refreshToken = "refresh-token"

        When("회원가입이 성공하면") {
            val response = mockk<HttpServletResponse>(relaxed = true)
            every { authFacade.signUp(request) } returns Pair(signUpResponse, refreshToken)
            every { response.setHeader(any(), any()) } returns Unit

            val result = authController.signUp(request, response)

            Then("ApiResponse를 반환하고 쿠키를 설정한다") {
                result.success shouldBe true
                result.data?.accessToken shouldBe "access-token"
                verify(exactly = 1) { authFacade.signUp(request) }
                verify(exactly = 1) { response.setHeader("Set-Cookie", any()) }
            }
        }
    }

    Given("현재 사용자 정보 조회 요청이 있을 때") {
        val user = User(
            id = "user-id",
            name = "Test User",
            email = "test@example.com",
            password = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = true,
        )
        val meResponse = AuthMeResponseDto(
            id = "user-id",
            name = "Test User",
            email = "test@example.com",
        )

        When("사용자가 있으면") {
            every { authFacade.getMe(user) } returns meResponse

            val result = authController.me(user)

            Then("ApiResponse를 반환한다") {
                result.success shouldBe true
                result.data?.id shouldBe "user-id"
                result.data?.name shouldBe "Test User"
                result.data?.email shouldBe "test@example.com"
                verify(exactly = 1) { authFacade.getMe(user) }
            }
        }
    }

    Given("로그아웃 요청이 있을 때") {
        val refreshToken = "refresh-token"

        When("리프레시 토큰이 있으면") {
            clearMocks(authFacade)
            val response = mockk<HttpServletResponse>(relaxed = true)
            every { authFacade.logout(refreshToken) } returns Unit
            every { response.setHeader(any(), any()) } returns Unit

            val result = authController.logout(refreshToken, response)

            Then("리프레시 토큰이 있으면 ApiResponse를 반환하고 쿠키를 삭제한다") {
                result.success shouldBe true
                verify(exactly = 1) { authFacade.logout(refreshToken) }
                verify(exactly = 1) { response.setHeader("Set-Cookie", any()) }
            }
        }

        When("리프레시 토큰이 없으면") {
            clearMocks(authFacade)
            val response = mockk<HttpServletResponse>(relaxed = true)
            every { response.setHeader(any(), any()) } returns Unit

            val result = authController.logout(null, response)

            Then("리프레시 토큰 없이도 ApiResponse를 반환하고 쿠키를 삭제한다") {
                result.success shouldBe true
                verify(exactly = 0) { authFacade.logout(any()) }
                verify(exactly = 1) { response.setHeader("Set-Cookie", any()) }
            }
        }
    }

    Given("토큰 갱신 요청이 있을 때") {
        val refreshToken = "refresh-token"
        val refreshResponse = AuthRefreshResponseDto(accessToken = "new-access-token")

        When("리프레시 토큰이 있으면") {
            every { authFacade.refresh(refreshToken) } returns refreshResponse

            val result = authController.refresh(refreshToken)

            Then("ApiResponse를 반환한다") {
                result.success shouldBe true
                result.data?.accessToken shouldBe "new-access-token"
                verify(exactly = 1) { authFacade.refresh(refreshToken) }
            }
        }
    }
})
