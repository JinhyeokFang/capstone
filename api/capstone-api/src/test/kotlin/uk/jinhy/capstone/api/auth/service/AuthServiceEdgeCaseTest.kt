package uk.jinhy.capstone.api.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.crypto.password.PasswordEncoder
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginDto
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.infra.auth.RefreshTokenBlocklistService
import uk.jinhy.capstone.util.exception.BadRequestException
import uk.jinhy.capstone.util.jwt.JwtUtil
import java.time.Instant

class AuthServiceEdgeCaseTest : BehaviorSpec({

    val userRepository = mockk<UserRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val refreshTokenBlocklistService = mockk<RefreshTokenBlocklistService>()

    val authService: AuthService = AuthServiceImpl(
        userRepository = userRepository,
        passwordEncoder = passwordEncoder,
        refreshTokenBlocklistService = refreshTokenBlocklistService
    )

    beforeSpec {
        JwtUtil.initialize(
            secretKey = "test-secret-key-for-testing-purposes-only-minimum-256-bits",
            accessTokenExpirationMillis = 600000,
            refreshTokenExpirationMillis = 604800000
        )
    }

    Given("사용자 비밀번호가 null인 경우") {
        val email = "test@example.com"
        val password = "password123"
        val now = Instant.now()

        val loginDto = AuthLoginDto(
            email = email,
            password = password
        )

        When("비밀번호가 null인 사용자로 로그인하면") {
            val user = User(
                id = "user-id",
                name = "Test User",
                email = email,
                password = null,
                createdAt = now,
                updatedAt = now,
                lastLoginAt = null,
                isActive = true
            )

            every { userRepository.findUserByEmail(email) } returns user

            Then("BadRequestException을 발생시킨다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }

                exception.message shouldBe "비밀번호가 일치하지 않습니다."
                exception.code shouldBe "PASSWORD_MISMATCH"
            }
        }
    }

    Given("공백 문자가 포함된 이메일로 로그인할 때") {
        val email = "  test@example.com  "
        val password = "password123"
        val encodedPassword = "encodedPassword"
        val now = Instant.now()

        val loginDto = AuthLoginDto(
            email = email,
            password = password
        )

        When("공백이 포함된 이메일로 조회하면") {
            every { userRepository.findUserByEmail(email) } returns null

            Then("사용자를 찾을 수 없다는 예외를 발생시킨다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }

                exception.message shouldBe "사용자를 찾을 수 없습니다."
                exception.code shouldBe "USER_NOT_FOUND"
            }
        }
    }

    Given("특수 문자가 포함된 비밀번호") {
        val email = "test@example.com"
        val password = "p@ssw0rd!@#$%^&*()"
        val encodedPassword = "encodedPassword"
        val userId = "user-id-123"
        val now = Instant.now()

        val loginDto = AuthLoginDto(
            email = email,
            password = password
        )

        When("특수 문자가 포함된 비밀번호로 로그인하면") {
            val user = User(
                id = userId,
                name = "Test User",
                email = email,
                password = encodedPassword,
                createdAt = now,
                updatedAt = now,
                lastLoginAt = null,
                isActive = true
            )

            every { userRepository.findUserByEmail(email) } returns user
            every { passwordEncoder.matches(password, encodedPassword) } returns true
            every { userRepository.save(any()) } returns user.login()

            Then("정상적으로 로그인된다") {
                val result = authService.login(loginDto)

                result.accessToken.isNotBlank() shouldBe true
                result.refreshToken.isNotBlank() shouldBe true
            }
        }
    }

    Given("대소문자가 다른 이메일") {
        val email = "Test@Example.Com"
        val password = "password123"

        val loginDto = AuthLoginDto(
            email = email,
            password = password
        )

        When("대소문자가 다른 이메일로 조회하면") {
            every { userRepository.findUserByEmail(email) } returns null

            Then("사용자를 찾을 수 없다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }

                exception.message shouldBe "사용자를 찾을 수 없습니다."
            }
        }
    }

    Given("빈 문자열 비밀번호") {
        val email = "test@example.com"
        val password = ""
        val now = Instant.now()

        val loginDto = AuthLoginDto(
            email = email,
            password = password
        )

        When("빈 비밀번호로 로그인하면") {
            val user = User(
                id = "user-id",
                name = "Test User",
                email = email,
                password = "encodedPassword",
                createdAt = now,
                updatedAt = now,
                lastLoginAt = null,
                isActive = true
            )

            every { userRepository.findUserByEmail(email) } returns user
            every { passwordEncoder.matches(password, "encodedPassword") } returns false

            Then("비밀번호 불일치 예외를 발생시킨다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }

                exception.message shouldBe "비밀번호가 일치하지 않습니다."
                exception.code shouldBe "PASSWORD_MISMATCH"
            }
        }
    }
})
