package uk.jinhy.capstone.api.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
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
    val userRepository = mockk<UserRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val refreshTokenBlocklistService = mockk<RefreshTokenBlocklistService>()
    val authService = AuthServiceImpl(
        userRepository = userRepository,
        passwordEncoder = passwordEncoder,
        refreshTokenBlocklistService = refreshTokenBlocklistService,
    )

    beforeSpec {
        JwtUtil.initialize(
            secretKey = "test-secret-key-for-testing-purposes-only-minimum-256-bits-required-for-hmac-sha-algorithm",
            accessTokenExpirationMillis = 600000,
            refreshTokenExpirationMillis = 604800000,
        )
    }

    Given("로그인 요청이 있을 때") {
        val email = "test@example.com"
        val password = "password123"
        val encodedPassword = "encodedPassword"
        val loginDto = AuthLoginDto(email = email, password = password)
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

        When("사용자가 존재하고 비밀번호가 일치하면") {
            clearMocks(userRepository, passwordEncoder, refreshTokenBlocklistService)
            val updatedUser = user.login()
            every { userRepository.findUserByEmail(email) } returns user
            every { passwordEncoder.matches(password, encodedPassword) } returns true
            every { userRepository.save(any()) } returns updatedUser

            val result = authService.login(loginDto)

            Then("로그인 성공 시 액세스·리프레시 토큰을 반환한다") {
                result.accessToken shouldNotBe null
                result.refreshToken shouldNotBe null
                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { passwordEncoder.matches(password, encodedPassword) }
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }

        When("사용자가 존재하지 않으면") {
            clearMocks(userRepository, passwordEncoder, refreshTokenBlocklistService)
            every { userRepository.findUserByEmail(email) } returns null

            Then("사용자가 없으면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }
                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
            }
        }

        When("비밀번호가 일치하지 않으면") {
            clearMocks(userRepository, passwordEncoder, refreshTokenBlocklistService)
            every { userRepository.findUserByEmail(email) } returns user
            every { passwordEncoder.matches(password, encodedPassword) } returns false

            Then("비밀번호 불일치 시 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }
                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { passwordEncoder.matches(password, encodedPassword) }
            }
        }

        When("사용자가 비활성화되어 있으면") {
            clearMocks(userRepository, passwordEncoder, refreshTokenBlocklistService)
            val inactiveUser = user.copy(isActive = false)
            every { userRepository.findUserByEmail(email) } returns inactiveUser
            every { passwordEncoder.matches(password, encodedPassword) } returns true

            Then("비활성 사용자면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    authService.login(loginDto)
                }
                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { passwordEncoder.matches(password, encodedPassword) }
            }
        }
    }

    Given("회원가입 요청이 있을 때") {
        val name = "Test User"
        val email = "test@example.com"
        val password = "password123"
        val encodedPassword = "encodedPassword"
        val signUpDto = AuthSignUpDto(name = name, email = email, password = password)
        val now = Instant.now()
        val newUser = User.create(
            name = name,
            email = email,
            password = encodedPassword,
            createdAt = now,
            updatedAt = now,
        )

        When("이메일이 중복되지 않으면") {
            clearMocks(userRepository, passwordEncoder, refreshTokenBlocklistService)
            every { userRepository.findUserByEmail(email) } returns null
            every { passwordEncoder.encode(password) } returns encodedPassword
            every { userRepository.save(any()) } returns newUser

            val result = authService.signUp(signUpDto)

            Then("액세스 토큰과 리프레시 토큰을 반환한다") {
                result.accessToken shouldNotBe null
                result.refreshToken shouldNotBe null
                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { passwordEncoder.encode(password) }
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }

        When("이메일이 이미 존재하면") {
            clearMocks(userRepository, passwordEncoder, refreshTokenBlocklistService)
            val existingUser = User(
                id = "existing-id",
                name = "Existing User",
                email = email,
                password = encodedPassword,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                lastLoginAt = null,
                isActive = true,
            )
            every { userRepository.findUserByEmail(email) } returns existingUser

            Then("ConflictException이 발생한다") {
                shouldThrow<ConflictException> {
                    authService.signUp(signUpDto)
                }
                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 0) { passwordEncoder.encode(any()) }
            }
        }
    }

    Given("로그아웃 요청이 있을 때") {
        val userId = "user-id"
        val email = "test@example.com"
        val refreshToken = JwtUtil.generateRefreshToken(
            subject = userId,
            claims = mapOf("email" to email),
        )

        When("유효한 리프레시 토큰으로 로그아웃하면") {
            clearMocks(userRepository, passwordEncoder, refreshTokenBlocklistService)
            every { refreshTokenBlocklistService.addToBlocklist(any(), any()) } returns Unit

            authService.logout(refreshToken)

            Then("블록리스트에 추가한다") {
                verify(exactly = 1) { refreshTokenBlocklistService.addToBlocklist(any(), any()) }
            }
        }
    }

    Given("토큰 갱신 요청이 있을 때") {
        val userId = "user-id"
        val email = "test@example.com"
        val refreshToken = JwtUtil.generateRefreshToken(
            subject = userId,
            claims = mapOf("email" to email),
        )

        When("유효한 리프레시 토큰으로 갱신하면") {
            clearMocks(userRepository, passwordEncoder, refreshTokenBlocklistService)
            every { refreshTokenBlocklistService.isBlocked(refreshToken) } returns false

            val result = authService.refresh(refreshToken)

            Then("새로운 액세스 토큰을 반환한다") {
                result shouldNotBe null
                verify(exactly = 1) { refreshTokenBlocklistService.isBlocked(refreshToken) }
            }
        }

        When("차단된 토큰이면") {
            clearMocks(userRepository, passwordEncoder, refreshTokenBlocklistService)
            every { refreshTokenBlocklistService.isBlocked(refreshToken) } returns true

            Then("UnauthorizedException이 발생한다") {
                shouldThrow<UnauthorizedException> {
                    authService.refresh(refreshToken)
                }
                verify(exactly = 1) { refreshTokenBlocklistService.isBlocked(refreshToken) }
            }
        }
    }
})
