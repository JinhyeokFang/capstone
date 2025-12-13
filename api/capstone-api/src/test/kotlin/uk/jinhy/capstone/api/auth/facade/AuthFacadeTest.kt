package uk.jinhy.capstone.api.auth.facade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.service.AuthService
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginDto
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginResultDto
import uk.jinhy.capstone.api.auth.service.dto.AuthSignUpDto
import uk.jinhy.capstone.api.auth.service.dto.AuthSignUpResultDto
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.util.jwt.JwtUtil
import java.time.Instant

class AuthFacadeTest : BehaviorSpec({
    val authService = mockk<AuthService>()
    val authFacade = AuthFacade(authService)

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
        val loginResult = AuthLoginResultDto(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )

        When("AuthService가 성공하면") {
            every { authService.login(any()) } returns loginResult

            val result = authFacade.login(request)

            Then("AuthLoginResponseDto와 refreshToken을 반환한다") {
                result.first.accessToken shouldBe "access-token"
                result.second shouldBe "refresh-token"
                verify(exactly = 1) { authService.login(any<AuthLoginDto>()) }
            }
        }
    }

    Given("회원가입 요청이 있을 때") {
        val request = AuthSignUpRequestDto(
            name = "Test User",
            email = "test@example.com",
            password = "password123",
        )
        val signUpResult = AuthSignUpResultDto(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )

        When("AuthService가 성공하면") {
            every { authService.signUp(any()) } returns signUpResult

            val result = authFacade.signUp(request)

            Then("AuthSignUpResponseDto와 refreshToken을 반환한다") {
                result.first.accessToken shouldBe "access-token"
                result.second shouldBe "refresh-token"
                verify(exactly = 1) { authService.signUp(any<AuthSignUpDto>()) }
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

        When("사용자가 있으면") {
            val result = authFacade.getMe(user)

            Then("AuthMeResponseDto를 반환한다") {
                result.id shouldBe "user-id"
                result.name shouldBe "Test User"
                result.email shouldBe "test@example.com"
            }
        }
    }

    Given("로그아웃 요청이 있을 때") {
        val refreshToken = "refresh-token"

        When("리프레시 토큰이 있으면") {
            every { authService.logout(refreshToken) } returns Unit

            authFacade.logout(refreshToken)

            Then("AuthService의 logout을 호출한다") {
                verify(exactly = 1) { authService.logout(refreshToken) }
            }
        }
    }

    Given("토큰 갱신 요청이 있을 때") {
        val refreshToken = "refresh-token"
        val newAccessToken = "new-access-token"

        When("유효한 리프레시 토큰이면") {
            every { authService.refresh(refreshToken) } returns newAccessToken

            val result = authFacade.refresh(refreshToken)

            Then("새로운 액세스 토큰을 반환한다") {
                result.accessToken shouldBe "new-access-token"
                verify(exactly = 1) { authService.refresh(refreshToken) }
            }
        }
    }
})
