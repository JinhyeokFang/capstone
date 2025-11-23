package uk.jinhy.capstone.api.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginDto
import uk.jinhy.capstone.api.auth.service.dto.AuthSignUpDto
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.infra.auth.RefreshTokenBlocklistService
import uk.jinhy.capstone.util.exception.BadRequestException
import uk.jinhy.capstone.util.exception.ConflictException
import uk.jinhy.capstone.util.exception.UnauthorizedException
import uk.jinhy.capstone.util.jwt.JwtUtil
import java.time.Instant

class AuthServiceTest : BehaviorSpec({

    // Mock dependencies
    val userRepository = mockk<UserRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val refreshTokenBlocklistService = mockk<RefreshTokenBlocklistService>()

    val authService: AuthService = AuthServiceImpl(
        userRepository = userRepository,
        passwordEncoder = passwordEncoder,
        refreshTokenBlocklistService = refreshTokenBlocklistService
    )

    beforeSpec {
        // JWT 초기화
        JwtUtil.initialize(
            secretKey = "test-secret-key-for-testing-purposes-only-minimum-256-bits",
            accessTokenExpirationMillis = 600000,
            refreshTokenExpirationMillis = 604800000
        )
    }

    Given("로그인 요청이 주어졌을 때") {
        val email = "test@example.com"
        val password = "password123"
        val encodedPassword = "encodedPassword"
        val userId = "user-id-123"
        val now = Instant.now()

        val loginDto = AuthLoginDto(
            email = email,
            password = password
        )

        When("올바른 이메일과 비밀번호로 로그인하면") {
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

            Then("액세스 토큰과 리프레시 토큰을 발급한다") {
                val result = authService.login(loginDto)

                result.accessToken.shouldNotBeBlank()
                result.refreshToken.shouldNotBeBlank()

                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { passwordEncoder.matches(password, encodedPassword) }
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }

        When("존재하지 않는 이메일로 로그인하면") {
            every { userRepository.findUserByEmail(email) } returns null

            Then("BadRequestException을 발생시킨다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }

                exception.message shouldBe "사용자를 찾을 수 없습니다."
                exception.code shouldBe "USER_NOT_FOUND"
            }
        }

        When("비밀번호가 일치하지 않으면") {
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
            every { passwordEncoder.matches(password, encodedPassword) } returns false

            Then("BadRequestException을 발생시킨다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }

                exception.message shouldBe "비밀번호가 일치하지 않습니다."
                exception.code shouldBe "PASSWORD_MISMATCH"
            }
        }

        When("비활성화된 사용자가 로그인하면") {
            val user = User(
                id = userId,
                name = "Test User",
                email = email,
                password = encodedPassword,
                createdAt = now,
                updatedAt = now,
                lastLoginAt = null,
                isActive = false
            )

            every { userRepository.findUserByEmail(email) } returns user
            every { passwordEncoder.matches(password, encodedPassword) } returns true

            Then("BadRequestException을 발생시킨다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }

                exception.message shouldBe "비활성화된 사용자입니다."
                exception.code shouldBe "USER_INACTIVE"
            }
        }
    }

    Given("회원가입 요청이 주어졌을 때") {
        val name = "Test User"
        val email = "newuser@example.com"
        val password = "password123"
        val encodedPassword = "encodedPassword"
        val now = Instant.now()

        val signUpDto = AuthSignUpDto(
            name = name,
            email = email,
            password = password
        )

        When("새로운 사용자가 회원가입하면") {
            val savedUser = User(
                id = "new-user-id",
                name = name,
                email = email,
                password = encodedPassword,
                createdAt = now,
                updatedAt = now,
                lastLoginAt = null,
                isActive = true
            )

            every { userRepository.findUserByEmail(email) } returns null
            every { passwordEncoder.encode(password) } returns encodedPassword
            every { userRepository.save(any()) } returns savedUser

            Then("액세스 토큰과 리프레시 토큰을 발급한다") {
                val result = authService.signUp(signUpDto)

                result.accessToken.shouldNotBeBlank()
                result.refreshToken.shouldNotBeBlank()

                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { passwordEncoder.encode(password) }
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }

        When("이미 존재하는 이메일로 회원가입하면") {
            val existingUser = User(
                id = "existing-user-id",
                name = "Existing User",
                email = email,
                password = encodedPassword,
                createdAt = now,
                updatedAt = now,
                lastLoginAt = null,
                isActive = true
            )

            every { userRepository.findUserByEmail(email) } returns existingUser

            Then("ConflictException을 발생시킨다") {
                val exception = shouldThrow<ConflictException> {
                    authService.signUp(signUpDto)
                }

                exception.message shouldBe "이미 존재하는 이메일입니다."
                exception.code shouldBe "EMAIL_ALREADY_EXISTS"
            }
        }
    }

    Given("로그아웃 요청이 주어졌을 때") {
        val userId = "user-id-123"
        val refreshToken = JwtUtil.generateRefreshToken(
            subject = userId,
            claims = mapOf("email" to "test@example.com")
        )

        When("유효한 리프레시 토큰으로 로그아웃하면") {
            every { refreshTokenBlocklistService.addToBlocklist(any(), any()) } returns Unit

            Then("리프레시 토큰을 블록리스트에 추가한다") {
                authService.logout(refreshToken)

                verify(exactly = 1) { refreshTokenBlocklistService.addToBlocklist(refreshToken, any()) }
            }
        }

        When("유효하지 않은 토큰으로 로그아웃하면") {
            val invalidToken = "invalid.token.here"

            Then("UnauthorizedException을 발생시킨다") {
                shouldThrow<UnauthorizedException> {
                    authService.logout(invalidToken)
                }
            }
        }

        When("액세스 토큰으로 로그아웃하면") {
            val accessToken = JwtUtil.generateAccessToken(
                subject = userId,
                claims = mapOf("email" to "test@example.com")
            )

            Then("UnauthorizedException을 발생시킨다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.logout(accessToken)
                }

                exception.message shouldBe "리프레시 토큰이 아닙니다."
            }
        }
    }

    Given("토큰 갱신 요청이 주어졌을 때") {
        val userId = "user-id-123"
        val email = "test@example.com"
        val refreshToken = JwtUtil.generateRefreshToken(
            subject = userId,
            claims = mapOf("email" to email)
        )

        When("유효한 리프레시 토큰으로 갱신하면") {
            every { refreshTokenBlocklistService.isBlocked(refreshToken) } returns false

            Then("새로운 액세스 토큰을 발급한다") {
                val newAccessToken = authService.refresh(refreshToken)

                newAccessToken.shouldNotBeBlank()
                JwtUtil.extractSubject(newAccessToken) shouldBe userId
                JwtUtil.extractClaim(newAccessToken, "email") shouldBe email
                JwtUtil.getTokenType(newAccessToken) shouldBe JwtUtil.TokenType.ACCESS

                verify(exactly = 1) { refreshTokenBlocklistService.isBlocked(refreshToken) }
            }
        }

        When("유효하지 않은 토큰으로 갱신하면") {
            val invalidToken = "invalid.token.here"

            Then("UnauthorizedException을 발생시킨다") {
                shouldThrow<UnauthorizedException> {
                    authService.refresh(invalidToken)
                }
            }
        }

        When("블록리스트에 있는 토큰으로 갱신하면") {
            every { refreshTokenBlocklistService.isBlocked(refreshToken) } returns true

            Then("UnauthorizedException을 발생시킨다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.refresh(refreshToken)
                }

                exception.message shouldBe "차단된 토큰입니다."
            }
        }

        When("액세스 토큰으로 갱신하면") {
            val accessToken = JwtUtil.generateAccessToken(
                subject = userId,
                claims = mapOf("email" to email)
            )

            Then("UnauthorizedException을 발생시킨다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.refresh(accessToken)
                }

                exception.message shouldBe "리프레시 토큰이 아닙니다."
            }
        }
    }
})
