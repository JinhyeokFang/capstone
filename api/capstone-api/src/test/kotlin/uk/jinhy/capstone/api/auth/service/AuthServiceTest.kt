package uk.jinhy.capstone.api.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
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

    // JwtUtil 초기화
    beforeSpec {
        JwtUtil.initialize(
            secretKey = "test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm",
            accessTokenExpirationMillis = 600000,
            refreshTokenExpirationMillis = 604800000,
        )
    }

    lateinit var userRepository: UserRepository
    lateinit var passwordEncoder: PasswordEncoder
    lateinit var refreshTokenBlocklistService: RefreshTokenBlocklistService
    lateinit var authService: AuthService

    beforeEach {
        clearAllMocks()
        userRepository = mockk<UserRepository>()
        passwordEncoder = mockk<PasswordEncoder>()
        refreshTokenBlocklistService = mockk<RefreshTokenBlocklistService>()
        authService = AuthServiceImpl(userRepository, passwordEncoder, refreshTokenBlocklistService)
    }

    Given("유효한 이메일과 비밀번호가 주어졌을 때") {
        val email = "test@example.com"
        val password = "password123"
        val encodedPassword = "encodedPassword123"
        val user = User(
            id = "user-id",
            name = "Test User",
            email = email,
            password = encodedPassword,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = true,
        )

        When("로그인을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns user
            every { passwordEncoder.matches(password, encodedPassword) } returns true
            every { userRepository.save(any()) } returns user.login()

            val result = authService.login(AuthLoginDto(email, password))

            Then("액세스 토큰과 리프레시 토큰이 반환된다") {
                result.accessToken shouldNotBe null
                result.refreshToken shouldNotBe null
            }

            Then("사용자 정보가 업데이트된다") {
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }
    }

    Given("존재하지 않는 이메일로") {
        val email = "nonexistent@example.com"
        val password = "password123"

        When("로그인을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns null

            Then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(AuthLoginDto(email, password))
                }
                exception.message shouldBe "사용자를 찾을 수 없습니다."
                exception.errorCode shouldBe "USER_NOT_FOUND"
            }
        }
    }

    Given("잘못된 비밀번호로") {
        val email = "test@example.com"
        val password = "wrongPassword"
        val encodedPassword = "encodedPassword123"
        val user = User(
            id = "user-id",
            name = "Test User",
            email = email,
            password = encodedPassword,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = true,
        )

        When("로그인을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns user
            every { passwordEncoder.matches(password, encodedPassword) } returns false

            Then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(AuthLoginDto(email, password))
                }
                exception.message shouldBe "비밀번호가 일치하지 않습니다."
                exception.errorCode shouldBe "PASSWORD_MISMATCH"
            }
        }
    }

    Given("비활성화된 사용자로") {
        val email = "test@example.com"
        val password = "password123"
        val encodedPassword = "encodedPassword123"
        val user = User(
            id = "user-id",
            name = "Test User",
            email = email,
            password = encodedPassword,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = false,
        )

        When("로그인을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns user
            every { passwordEncoder.matches(password, encodedPassword) } returns true

            Then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(AuthLoginDto(email, password))
                }
                exception.message shouldBe "비활성화된 사용자입니다."
                exception.errorCode shouldBe "USER_INACTIVE"
            }
        }
    }

    Given("새로운 사용자 정보가 주어졌을 때") {
        val name = "New User"
        val email = "newuser@example.com"
        val password = "password123"
        val encodedPassword = "encodedPassword123"

        When("회원가입을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns null
            every { passwordEncoder.encode(password) } returns encodedPassword
            every { userRepository.save(any()) } answers {
                val user = firstArg<User>()
                user
            }

            val result = authService.signUp(AuthSignUpDto(name, email, password))

            Then("액세스 토큰과 리프레시 토큰이 반환된다") {
                result.accessToken shouldNotBe null
                result.refreshToken shouldNotBe null
            }

            Then("사용자가 저장된다") {
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }
    }

    Given("이미 존재하는 이메일로") {
        val name = "New User"
        val email = "existing@example.com"
        val password = "password123"
        val existingUser = User(
            id = "existing-user-id",
            name = "Existing User",
            email = email,
            password = "encodedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = true,
        )

        When("회원가입을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns existingUser

            Then("ConflictException이 발생한다") {
                val exception = shouldThrow<ConflictException> {
                    authService.signUp(AuthSignUpDto(name, email, password))
                }
                exception.message shouldBe "이미 존재하는 이메일입니다."
                exception.errorCode shouldBe "EMAIL_ALREADY_EXISTS"
            }
        }
    }

    Given("유효한 리프레시 토큰이 주어졌을 때") {
        val userId = "user-id"
        val email = "test@example.com"
        val refreshToken = JwtUtil.generateRefreshToken(
            subject = userId,
            claims = mapOf("email" to email),
        )

        When("로그아웃을 시도하면") {
            every { refreshTokenBlocklistService.addToBlocklist(any(), any()) } returns Unit

            authService.logout(refreshToken)

            Then("토큰이 블록리스트에 추가된다") {
                verify(exactly = 1) { refreshTokenBlocklistService.addToBlocklist(refreshToken, any()) }
            }
        }
    }

    Given("유효하지 않은 리프레시 토큰으로") {
        val invalidToken = "invalid.token.here"

        When("로그아웃을 시도하면") {
            Then("UnauthorizedException이 발생한다") {
                shouldThrow<UnauthorizedException> {
                    authService.logout(invalidToken)
                }
            }
        }
    }

    Given("액세스 토큰으로") {
        val userId = "user-id"
        val email = "test@example.com"
        val accessToken = JwtUtil.generateAccessToken(
            subject = userId,
            claims = mapOf("email" to email),
        )

        When("로그아웃을 시도하면") {
            Then("UnauthorizedException이 발생한다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.logout(accessToken)
                }
                exception.message shouldBe "리프레시 토큰이 아닙니다."
            }
        }
    }

    Given("유효한 리프레시 토큰이 주어졌을 때") {
        val userId = "user-id"
        val email = "test@example.com"
        val refreshToken = JwtUtil.generateRefreshToken(
            subject = userId,
            claims = mapOf("email" to email),
        )

        When("토큰 갱신을 시도하면") {
            every { refreshTokenBlocklistService.isBlocked(refreshToken) } returns false

            val newAccessToken = authService.refresh(refreshToken)

            Then("새로운 액세스 토큰이 반환된다") {
                newAccessToken shouldNotBe null
                JwtUtil.extractSubject(newAccessToken) shouldBe userId
                JwtUtil.extractClaim(newAccessToken, "email") shouldBe email
            }
        }
    }

    Given("블록리스트에 있는 리프레시 토큰으로") {
        val userId = "user-id"
        val email = "test@example.com"
        val refreshToken = JwtUtil.generateRefreshToken(
            subject = userId,
            claims = mapOf("email" to email),
        )

        When("토큰 갱신을 시도하면") {
            every { refreshTokenBlocklistService.isBlocked(refreshToken) } returns true

            Then("UnauthorizedException이 발생한다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.refresh(refreshToken)
                }
                exception.message shouldBe "차단된 토큰입니다."
            }
        }
    }

    Given("액세스 토큰으로") {
        val userId = "user-id"
        val email = "test@example.com"
        val accessToken = JwtUtil.generateAccessToken(
            subject = userId,
            claims = mapOf("email" to email),
        )

        When("토큰 갱신을 시도하면") {
            Then("UnauthorizedException이 발생한다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.refresh(accessToken)
                }
                exception.message shouldBe "리프레시 토큰이 아닙니다."
            }
        }
    }

    Given("유효하지 않은 토큰으로") {
        val invalidToken = "invalid.token.here"

        When("토큰 갱신을 시도하면") {
            Then("UnauthorizedException이 발생한다") {
                shouldThrow<UnauthorizedException> {
                    authService.refresh(invalidToken)
                }
            }
        }
    }
})
