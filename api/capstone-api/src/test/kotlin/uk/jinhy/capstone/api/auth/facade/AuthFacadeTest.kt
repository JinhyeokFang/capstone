package uk.jinhy.capstone.api.auth.facade

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
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

    lateinit var authFacade: AuthFacade

    beforeEach {
        authFacade = AuthFacade(authService)
    }

    given("회원가입 Facade") {
        `when`("회원가입 요청이 주어지면") {
            val request = AuthSignUpRequestDto(
                name = "테스트 유저",
                email = "test@example.com",
                password = "password123",
            )

            val serviceResult = AuthSignUpResultDto(
                accessToken = "test-access-token",
                refreshToken = "test-refresh-token",
            )

            every {
                authService.signUp(
                    AuthSignUpDto(
                        name = request.name,
                        email = request.email,
                        password = request.password,
                    ),
                )
            } returns serviceResult

            then("AuthService를 호출하고 응답 DTO와 리프레시 토큰을 반환한다") {
                val (response, refreshToken) = authFacade.signUp(request)

                response.accessToken shouldBe "test-access-token"
                refreshToken shouldBe "test-refresh-token"

                verify(exactly = 1) {
                    authService.signUp(
                        AuthSignUpDto(
                            name = request.name,
                            email = request.email,
                            password = request.password,
                        ),
                    )
                }
            }
        }
    }

    given("로그인 Facade") {
        `when`("로그인 요청이 주어지면") {
            val request = AuthLoginRequestDto(
                email = "test@example.com",
                password = "password123",
            )

            val serviceResult = AuthLoginResultDto(
                accessToken = "test-access-token",
                refreshToken = "test-refresh-token",
            )

            every {
                authService.login(
                    AuthLoginDto(
                        email = request.email,
                        password = request.password,
                    ),
                )
            } returns serviceResult

            then("AuthService를 호출하고 응답 DTO와 리프레시 토큰을 반환한다") {
                val (response, refreshToken) = authFacade.login(request)

                response.accessToken shouldBe "test-access-token"
                refreshToken shouldBe "test-refresh-token"

                verify(exactly = 1) {
                    authService.login(
                        AuthLoginDto(
                            email = request.email,
                            password = request.password,
                        ),
                    )
                }
            }
        }
    }

    given("현재 사용자 정보 조회 Facade") {
        `when`("사용자 객체가 주어지면") {
            val user = User(
                id = "user-id",
                name = "테스트 유저",
                email = "test@example.com",
                password = "encoded-password",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                lastLoginAt = Instant.now(),
                isActive = true,
            )

            then("사용자 정보를 응답 DTO로 변환하여 반환한다") {
                val response = authFacade.getMe(user)

                response.id shouldBe "user-id"
                response.name shouldBe "테스트 유저"
                response.email shouldBe "test@example.com"
            }
        }
    }

    given("로그아웃 Facade") {
        `when`("리프레시 토큰이 주어지면") {
            val refreshToken = "test-refresh-token"

            every { authService.logout(refreshToken) } returns Unit

            then("AuthService의 logout을 호출한다") {
                authFacade.logout(refreshToken)

                verify(exactly = 1) { authService.logout(refreshToken) }
            }
        }
    }

    given("토큰 갱신 Facade") {
        `when`("리프레시 토큰이 주어지면") {
            val refreshToken = "test-refresh-token"
            val newAccessToken = "new-access-token"

            every { authService.refresh(refreshToken) } returns newAccessToken

            then("AuthService를 호출하고 새로운 액세스 토큰을 반환한다") {
                val response = authFacade.refresh(refreshToken)

                response.accessToken shouldBe newAccessToken

                verify(exactly = 1) { authService.refresh(refreshToken) }
            }
        }
    }
})
