package uk.jinhy.capstone.api.auth.facade

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.service.AuthService
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginDto
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginResultDto
import uk.jinhy.capstone.api.auth.service.dto.AuthSignUpDto
import uk.jinhy.capstone.api.auth.service.dto.AuthSignUpResultDto
import uk.jinhy.capstone.domain.user.model.User
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
class AuthFacadeTest(
    @MockkBean private val authService: AuthService,
) : BehaviorSpec({

    val authFacade = AuthFacade(authService = authService)

    Given("로그인 요청이 주어졌을 때") {
        val request = AuthLoginRequestDto(
            email = "test@example.com",
            password = "password123",
        )

        val accessToken = "access.token.here"
        val refreshToken = "refresh.token.here"

        When("로그인을 수행하면") {
            every { authService.login(any()) } returns AuthLoginResultDto(
                accessToken = accessToken,
                refreshToken = refreshToken,
            )

            val (loginResponse, returnedRefreshToken) = authFacade.login(request)

            Then("로그인 응답과 리프레시 토큰을 반환한다") {
                loginResponse.accessToken shouldBe accessToken
                returnedRefreshToken shouldBe refreshToken

                verify(exactly = 1) {
                    authService.login(
                        match {
                            it.email == request.email && it.password == request.password
                        },
                    )
                }
            }
        }
    }

    Given("회원가입 요청이 주어졌을 때") {
        val request = AuthSignUpRequestDto(
            name = "Test User",
            email = "test@example.com",
            password = "password123",
        )

        val accessToken = "access.token.here"
        val refreshToken = "refresh.token.here"

        When("회원가입을 수행하면") {
            every { authService.signUp(any()) } returns AuthSignUpResultDto(
                accessToken = accessToken,
                refreshToken = refreshToken,
            )

            val (signUpResponse, returnedRefreshToken) = authFacade.signUp(request)

            Then("회원가입 응답과 리프레시 토큰을 반환한다") {
                signUpResponse.accessToken shouldBe accessToken
                returnedRefreshToken shouldBe refreshToken

                verify(exactly = 1) {
                    authService.signUp(
                        match {
                            it.name == request.name &&
                                it.email == request.email &&
                                it.password == request.password
                        },
                    )
                }
            }
        }
    }

    Given("현재 사용자 정보가 주어졌을 때") {
        val now = Instant.now()
        val user = User(
            id = "user-id",
            name = "Test User",
            email = "test@example.com",
            password = "encoded-password",
            createdAt = now,
            updatedAt = now,
            lastLoginAt = now,
            isActive = true,
        )

        When("사용자 정보를 조회하면") {
            val response = authFacade.getMe(user)

            Then("사용자 정보를 반환한다") {
                response.id shouldBe user.id
                response.name shouldBe user.name
                response.email shouldBe user.email
            }
        }
    }

    Given("리프레시 토큰이 주어졌을 때") {
        val refreshToken = "refresh.token.here"

        When("로그아웃을 수행하면") {
            every { authService.logout(refreshToken) } returns Unit

            authFacade.logout(refreshToken)

            Then("AuthService의 logout이 호출된다") {
                verify(exactly = 1) { authService.logout(refreshToken) }
            }
        }

        When("토큰 갱신을 수행하면") {
            val newAccessToken = "new.access.token.here"
            every { authService.refresh(refreshToken) } returns newAccessToken

            val response = authFacade.refresh(refreshToken)

            Then("새로운 액세스 토큰을 반환한다") {
                response.accessToken shouldBe newAccessToken
                verify(exactly = 1) { authService.refresh(refreshToken) }
            }
        }
    }
})
