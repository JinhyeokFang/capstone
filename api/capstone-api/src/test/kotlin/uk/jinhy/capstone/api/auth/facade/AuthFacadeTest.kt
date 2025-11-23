package uk.jinhy.capstone.api.auth.facade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import java.time.Instant

class AuthFacadeTest : BehaviorSpec({

    val authService = mockk<AuthService>()
    val authFacade = AuthFacade(authService)

    Given("유효한 로그인 요청이 주어졌을 때") {
        val request = AuthLoginRequestDto(
            email = "test@example.com",
            password = "password123",
        )
        val loginResult = AuthLoginResultDto(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )

        When("로그인을 수행하면") {
            every { authService.login(any()) } returns loginResult

            val (response, refreshToken) = authFacade.login(request)

            Then("로그인 응답과 리프레시 토큰이 반환된다") {
                response.accessToken shouldBe "access-token"
                refreshToken shouldBe "refresh-token"
            }

            Then("AuthService의 login 메서드가 호출된다") {
                verify(exactly = 1) {
                    authService.login(
                        match { dto ->
                            dto.email == request.email && dto.password == request.password
                        },
                    )
                }
            }
        }
    }

    Given("유효한 회원가입 요청이 주어졌을 때") {
        val request = AuthSignUpRequestDto(
            name = "Test User",
            email = "test@example.com",
            password = "password123",
        )
        val signUpResult = AuthSignUpResultDto(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )

        When("회원가입을 수행하면") {
            every { authService.signUp(any()) } returns signUpResult

            val (response, refreshToken) = authFacade.signUp(request)

            Then("회원가입 응답과 리프레시 토큰이 반환된다") {
                response.accessToken shouldBe "access-token"
                refreshToken shouldBe "refresh-token"
            }

            Then("AuthService의 signUp 메서드가 호출된다") {
                verify(exactly = 1) {
                    authService.signUp(
                        match { dto ->
                            dto.name == request.name &&
                                dto.email == request.email &&
                                dto.password == request.password
                        },
                    )
                }
            }
        }
    }

    Given("사용자 정보가 주어졌을 때") {
        val user = User(
            id = "user-id",
            name = "Test User",
            email = "test@example.com",
            password = "password",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = Instant.now(),
            isActive = true,
        )

        When("사용자 정보를 조회하면") {
            val response = authFacade.getMe(user)

            Then("사용자 정보가 반환된다") {
                response.id shouldBe user.id
                response.name shouldBe user.name
                response.email shouldBe user.email
            }
        }
    }

    Given("리프레시 토큰이 주어졌을 때") {
        val refreshToken = "refresh-token"

        When("로그아웃을 수행하면") {
            every { authService.logout(refreshToken) } returns Unit

            authFacade.logout(refreshToken)

            Then("AuthService의 logout 메서드가 호출된다") {
                verify(exactly = 1) { authService.logout(refreshToken) }
            }
        }
    }

    Given("리프레시 토큰이 주어졌을 때") {
        val refreshToken = "refresh-token"
        val newAccessToken = "new-access-token"

        When("토큰 갱신을 수행하면") {
            every { authService.refresh(refreshToken) } returns newAccessToken

            val response = authFacade.refresh(refreshToken)

            Then("새로운 액세스 토큰이 반환된다") {
                response.accessToken shouldBe newAccessToken
            }

            Then("AuthService의 refresh 메서드가 호출된다") {
                verify(exactly = 1) { authService.refresh(refreshToken) }
            }
        }
    }
})
