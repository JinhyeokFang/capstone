package uk.jinhy.capstone.api.auth.facade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.service.AuthService
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginResultDto
import uk.jinhy.capstone.api.auth.service.dto.AuthSignUpResultDto
import uk.jinhy.capstone.domain.user.model.User
import java.time.Instant

class AuthFacadeTest : BehaviorSpec({

    val authService = mockk<AuthService>()
    val authFacade = AuthFacade(authService)

    Given("로그인 요청이 주어졌을 때") {
        val request = AuthLoginRequestDto(
            email = "test@example.com",
            password = "password123"
        )

        val accessToken = "access.token.here"
        val refreshToken = "refresh.token.here"

        When("로그인을 수행하면") {
            every {
                authService.login(any())
            } returns AuthLoginResultDto(
                accessToken = accessToken,
                refreshToken = refreshToken
            )

            Then("액세스 토큰과 리프레시 토큰을 반환한다") {
                val (response, returnedRefreshToken) = authFacade.login(request)

                response.accessToken shouldBe accessToken
                returnedRefreshToken shouldBe refreshToken

                verify(exactly = 1) { authService.login(any()) }
            }
        }
    }

    Given("회원가입 요청이 주어졌을 때") {
        val request = AuthSignUpRequestDto(
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )

        val accessToken = "access.token.here"
        val refreshToken = "refresh.token.here"

        When("회원가입을 수행하면") {
            every {
                authService.signUp(any())
            } returns AuthSignUpResultDto(
                accessToken = accessToken,
                refreshToken = refreshToken
            )

            Then("액세스 토큰과 리프레시 토큰을 반환한다") {
                val (response, returnedRefreshToken) = authFacade.signUp(request)

                response.accessToken shouldBe accessToken
                returnedRefreshToken shouldBe refreshToken

                verify(exactly = 1) { authService.signUp(any()) }
            }
        }
    }

    Given("현재 사용자가 주어졌을 때") {
        val userId = "user-id-123"
        val userName = "Test User"
        val userEmail = "test@example.com"
        val now = Instant.now()

        val user = User(
            id = userId,
            name = userName,
            email = userEmail,
            password = "encodedPassword",
            createdAt = now,
            updatedAt = now,
            lastLoginAt = now,
            isActive = true
        )

        When("사용자 정보를 조회하면") {
            Then("사용자 정보를 반환한다") {
                val response = authFacade.getMe(user)

                response.id shouldBe userId
                response.name shouldBe userName
                response.email shouldBe userEmail
            }
        }
    }

    Given("로그아웃 요청이 주어졌을 때") {
        val refreshToken = "refresh.token.here"

        When("로그아웃을 수행하면") {
            every { authService.logout(refreshToken) } returns Unit

            Then("로그아웃이 성공한다") {
                authFacade.logout(refreshToken)

                verify(exactly = 1) { authService.logout(refreshToken) }
            }
        }
    }

    Given("토큰 갱신 요청이 주어졌을 때") {
        val refreshToken = "refresh.token.here"
        val newAccessToken = "new.access.token.here"

        When("토큰을 갱신하면") {
            every { authService.refresh(refreshToken) } returns newAccessToken

            Then("새로운 액세스 토큰을 반환한다") {
                val response = authFacade.refresh(refreshToken)

                response.accessToken shouldBe newAccessToken

                verify(exactly = 1) { authService.refresh(refreshToken) }
            }
        }
    }
})
