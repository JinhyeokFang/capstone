package uk.jinhy.capstone.api.auth.presentation

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.Cookie
import org.springframework.mock.web.MockHttpServletResponse
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthLoginResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthRefreshResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthSignUpResponseDto
import uk.jinhy.capstone.api.auth.facade.AuthFacade
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.util.exception.UnauthorizedException
import java.time.Instant

class AuthControllerTest : BehaviorSpec({

    val authFacade = mockk<AuthFacade>()
    val authController = AuthController(authFacade)

    Given("유효한 로그인 요청이 주어졌을 때") {
        val request = AuthLoginRequestDto(
            email = "test@example.com",
            password = "password123",
        )
        val loginResponse = AuthLoginResponseDto(accessToken = "access-token")
        val refreshToken = "refresh-token"
        val response = MockHttpServletResponse()

        When("로그인을 수행하면") {
            every { authFacade.login(request) } returns Pair(loginResponse, refreshToken)

            val result = authController.login(request, response)

            Then("성공 응답이 반환된다") {
                result.success shouldBe true
                result.data shouldBe loginResponse
            }

            Then("리프레시 토큰이 쿠키로 설정된다") {
                val setCookieHeader = response.getHeader("Set-Cookie")
                setCookieHeader shouldNotBe null
                setCookieHeader!! shouldContain "refreshToken=$refreshToken"
                setCookieHeader shouldContain "HttpOnly"
                setCookieHeader shouldContain "Path=/"
            }
        }
    }

    Given("유효한 회원가입 요청이 주어졌을 때") {
        val request = AuthSignUpRequestDto(
            name = "Test User",
            email = "test@example.com",
            password = "password123",
        )
        val signUpResponse = AuthSignUpResponseDto(accessToken = "access-token")
        val refreshToken = "refresh-token"
        val response = MockHttpServletResponse()

        When("회원가입을 수행하면") {
            every { authFacade.signUp(request) } returns Pair(signUpResponse, refreshToken)

            val result = authController.signUp(request, response)

            Then("성공 응답이 반환된다") {
                result.success shouldBe true
                result.data shouldBe signUpResponse
            }

            Then("리프레시 토큰이 쿠키로 설정된다") {
                val setCookieHeader = response.getHeader("Set-Cookie")
                setCookieHeader shouldNotBe null
                setCookieHeader!! shouldContain "refreshToken=$refreshToken"
                setCookieHeader shouldContain "HttpOnly"
                setCookieHeader shouldContain "Path=/"
            }
        }
    }

    Given("인증된 사용자가 주어졌을 때") {
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
        val meResponse = uk.jinhy.capstone.api.auth.api.dto.response.AuthMeResponseDto(
            id = user.id,
            name = user.name,
            email = user.email,
        )

        When("사용자 정보를 조회하면") {
            every { authFacade.getMe(user) } returns meResponse

            val result = authController.me(user)

            Then("성공 응답이 반환된다") {
                result.success shouldBe true
                result.data shouldBe meResponse
            }
        }
    }

    Given("리프레시 토큰이 주어졌을 때") {
        val refreshToken = "refresh-token"
        val response = MockHttpServletResponse()

        When("로그아웃을 수행하면") {
            every { authFacade.logout(refreshToken) } returns Unit

            val result = authController.logout(refreshToken, response)

            Then("성공 응답이 반환된다") {
                result.success shouldBe true
            }

            Then("리프레시 토큰 쿠키가 삭제된다") {
                val setCookieHeader = response.getHeader("Set-Cookie")
                setCookieHeader shouldNotBe null
                setCookieHeader!! shouldContain "refreshToken="
                setCookieHeader shouldContain "Max-Age=0"
            }

            Then("AuthFacade의 logout 메서드가 호출된다") {
                verify(exactly = 1) { authFacade.logout(refreshToken) }
            }
        }
    }

    Given("리프레시 토큰이 없을 때") {
        val response = MockHttpServletResponse()

        When("로그아웃을 수행하면") {
            val result = authController.logout(null, response)

            Then("성공 응답이 반환된다") {
                result.success shouldBe true
            }

            Then("리프레시 토큰 쿠키가 삭제된다") {
                val setCookieHeader = response.getHeader("Set-Cookie")
                setCookieHeader shouldNotBe null
                setCookieHeader!! shouldContain "Max-Age=0"
            }

            Then("AuthFacade의 logout 메서드가 호출되지 않는다") {
                verify(exactly = 0) { authFacade.logout(any()) }
            }
        }
    }

    Given("유효한 리프레시 토큰이 주어졌을 때") {
        val refreshToken = "refresh-token"
        val newAccessToken = "new-access-token"
        val refreshResponse = AuthRefreshResponseDto(accessToken = newAccessToken)

        When("토큰 갱신을 수행하면") {
            every { authFacade.refresh(refreshToken) } returns refreshResponse

            val result = authController.refresh(refreshToken)

            Then("성공 응답이 반환된다") {
                result.success shouldBe true
                result.data shouldBe refreshResponse
            }
        }
    }

    Given("리프레시 토큰이 없을 때") {
        When("토큰 갱신을 시도하면") {
            Then("UnauthorizedException이 발생한다") {
                val exception = io.kotest.assertions.throwables.shouldThrow<UnauthorizedException> {
                    authController.refresh(null)
                }
                exception.message shouldBe "리프레시 토큰이 없습니다."
            }
        }
    }
})
