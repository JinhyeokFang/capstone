package uk.jinhy.capstone.api.auth.service

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.mockk.every
import io.mockk.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
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

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest(
    @MockkBean private val userRepository: UserRepository,
    @MockkBean private val passwordEncoder: PasswordEncoder,
    @MockkBean private val refreshTokenBlocklistService: RefreshTokenBlocklistService,
) : BehaviorSpec({

    lateinit var authService: AuthService

    beforeSpec {
        JwtUtil.initialize(
            secretKey = "test-secret-key-for-testing-purposes-only-minimum-256-bits",
            accessTokenExpirationMillis = 3600000,
            refreshTokenExpirationMillis = 604800000,
        )
    }

    beforeEach {
        authService = AuthServiceImpl(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder,
            refreshTokenBlocklistService = refreshTokenBlocklistService,
        )
    }

    given("회원가입") {
        `when`("유효한 회원가입 정보가 주어지면") {
            val dto = AuthSignUpDto(
                name = "테스트 유저",
                email = "test@example.com",
                password = "password123",
            )

            val encodedPassword = "encoded-password"
            val savedUser = User(
                id = "user-id",
                name = dto.name,
                email = dto.email,
                password = encodedPassword,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                lastLoginAt = null,
                isActive = true,
            )

            every { userRepository.findUserByEmail(dto.email) } returns null
            every { passwordEncoder.encode(dto.password) } returns encodedPassword
            every { userRepository.save(any()) } returns savedUser

            then("회원가입에 성공하고 액세스 토큰과 리프레시 토큰을 반환한다") {
                val result = authService.signUp(dto)

                result.accessToken.shouldNotBeEmpty()
                result.refreshToken.shouldNotBeEmpty()

                verify(exactly = 1) { userRepository.findUserByEmail(dto.email) }
                verify(exactly = 1) { passwordEncoder.encode(dto.password) }
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }

        `when`("이미 존재하는 이메일로 회원가입을 시도하면") {
            val dto = AuthSignUpDto(
                name = "테스트 유저",
                email = "existing@example.com",
                password = "password123",
            )

            val existingUser = User(
                id = "existing-user-id",
                name = "기존 유저",
                email = dto.email,
                password = "encoded-password",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                lastLoginAt = null,
                isActive = true,
            )

            every { userRepository.findUserByEmail(dto.email) } returns existingUser

            then("ConflictException을 던진다") {
                val exception = shouldThrow<ConflictException> {
                    authService.signUp(dto)
                }

                exception.message shouldBe "이미 존재하는 이메일입니다."
                exception.errorCode shouldBe "EMAIL_ALREADY_EXISTS"

                verify(exactly = 1) { userRepository.findUserByEmail(dto.email) }
                verify(exactly = 0) { passwordEncoder.encode(any()) }
                verify(exactly = 0) { userRepository.save(any()) }
            }
        }
    }

    given("로그인") {
        `when`("유효한 이메일과 비밀번호가 주어지면") {
            val dto = AuthLoginDto(
                email = "test@example.com",
                password = "password123",
            )

            val user = User(
                id = "user-id",
                name = "테스트 유저",
                email = dto.email,
                password = "encoded-password",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                lastLoginAt = null,
                isActive = true,
            )

            val updatedUser = user.login()

            every { userRepository.findUserByEmail(dto.email) } returns user
            every { passwordEncoder.matches(dto.password, user.password!!) } returns true
            every { userRepository.save(any()) } returns updatedUser

            then("로그인에 성공하고 액세스 토큰과 리프레시 토큰을 반환한다") {
                val result = authService.login(dto)

                result.accessToken.shouldNotBeEmpty()
                result.refreshToken.shouldNotBeEmpty()

                verify(exactly = 1) { userRepository.findUserByEmail(dto.email) }
                verify(exactly = 1) { passwordEncoder.matches(dto.password, user.password!!) }
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }

        `when`("존재하지 않는 이메일로 로그인을 시도하면") {
            val dto = AuthLoginDto(
                email = "nonexistent@example.com",
                password = "password123",
            )

            every { userRepository.findUserByEmail(dto.email) } returns null

            then("BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(dto)
                }

                exception.message shouldBe "사용자를 찾을 수 없습니다."
                exception.errorCode shouldBe "USER_NOT_FOUND"

                verify(exactly = 1) { userRepository.findUserByEmail(dto.email) }
                verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
            }
        }

        `when`("잘못된 비밀번호로 로그인을 시도하면") {
            val dto = AuthLoginDto(
                email = "test@example.com",
                password = "wrongpassword",
            )

            val user = User(
                id = "user-id",
                name = "테스트 유저",
                email = dto.email,
                password = "encoded-password",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                lastLoginAt = null,
                isActive = true,
            )

            every { userRepository.findUserByEmail(dto.email) } returns user
            every { passwordEncoder.matches(dto.password, user.password!!) } returns false

            then("BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(dto)
                }

                exception.message shouldBe "비밀번호가 일치하지 않습니다."
                exception.errorCode shouldBe "PASSWORD_MISMATCH"

                verify(exactly = 1) { userRepository.findUserByEmail(dto.email) }
                verify(exactly = 1) { passwordEncoder.matches(dto.password, user.password!!) }
                verify(exactly = 0) { userRepository.save(any()) }
            }
        }

        `when`("비활성화된 사용자로 로그인을 시도하면") {
            val dto = AuthLoginDto(
                email = "inactive@example.com",
                password = "password123",
            )

            val user = User(
                id = "user-id",
                name = "비활성화된 유저",
                email = dto.email,
                password = "encoded-password",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                lastLoginAt = null,
                isActive = false,
            )

            every { userRepository.findUserByEmail(dto.email) } returns user
            every { passwordEncoder.matches(dto.password, user.password!!) } returns true

            then("BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(dto)
                }

                exception.message shouldBe "비활성화된 사용자입니다."
                exception.errorCode shouldBe "USER_INACTIVE"

                verify(exactly = 1) { userRepository.findUserByEmail(dto.email) }
                verify(exactly = 1) { passwordEncoder.matches(dto.password, user.password!!) }
                verify(exactly = 0) { userRepository.save(any()) }
            }
        }
    }

    given("로그아웃") {
        `when`("유효한 리프레시 토큰으로 로그아웃하면") {
            val refreshToken = JwtUtil.generateRefreshToken(
                subject = "user-id",
                claims = mapOf("email" to "test@example.com"),
            )

            every { refreshTokenBlocklistService.addToBlocklist(refreshToken, any()) } returns Unit

            then("리프레시 토큰이 블록리스트에 추가된다") {
                authService.logout(refreshToken)

                verify(exactly = 1) { refreshTokenBlocklistService.addToBlocklist(refreshToken, any()) }
            }
        }

        `when`("유효하지 않은 토큰으로 로그아웃을 시도하면") {
            val invalidToken = "invalid-token"

            then("UnauthorizedException을 던진다") {
                shouldThrow<UnauthorizedException> {
                    authService.logout(invalidToken)
                }

                verify(exactly = 0) { refreshTokenBlocklistService.addToBlocklist(any(), any()) }
            }
        }

        `when`("액세스 토큰으로 로그아웃을 시도하면") {
            val accessToken = JwtUtil.generateAccessToken(
                subject = "user-id",
                claims = mapOf("email" to "test@example.com"),
            )

            then("UnauthorizedException을 던진다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.logout(accessToken)
                }

                exception.message shouldBe "리프레시 토큰이 아닙니다."

                verify(exactly = 0) { refreshTokenBlocklistService.addToBlocklist(any(), any()) }
            }
        }
    }

    given("토큰 갱신") {
        `when`("유효한 리프레시 토큰으로 갱신을 요청하면") {
            val refreshToken = JwtUtil.generateRefreshToken(
                subject = "user-id",
                claims = mapOf("email" to "test@example.com"),
            )

            every { refreshTokenBlocklistService.isBlocked(refreshToken) } returns false

            then("새로운 액세스 토큰을 반환한다") {
                val accessToken = authService.refresh(refreshToken)

                accessToken.shouldNotBeEmpty()

                val subject = JwtUtil.extractSubject(accessToken)
                subject shouldBe "user-id"

                val email = JwtUtil.extractClaim(accessToken, "email")
                email shouldBe "test@example.com"

                verify(exactly = 1) { refreshTokenBlocklistService.isBlocked(refreshToken) }
            }
        }

        `when`("유효하지 않은 토큰으로 갱신을 요청하면") {
            val invalidToken = "invalid-token"

            then("UnauthorizedException을 던진다") {
                shouldThrow<UnauthorizedException> {
                    authService.refresh(invalidToken)
                }

                verify(exactly = 0) { refreshTokenBlocklistService.isBlocked(any()) }
            }
        }

        `when`("액세스 토큰으로 갱신을 요청하면") {
            val accessToken = JwtUtil.generateAccessToken(
                subject = "user-id",
                claims = mapOf("email" to "test@example.com"),
            )

            then("UnauthorizedException을 던진다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.refresh(accessToken)
                }

                exception.message shouldBe "리프레시 토큰이 아닙니다."

                verify(exactly = 0) { refreshTokenBlocklistService.isBlocked(any()) }
            }
        }

        `when`("블록리스트에 있는 토큰으로 갱신을 요청하면") {
            val refreshToken = JwtUtil.generateRefreshToken(
                subject = "user-id",
                claims = mapOf("email" to "test@example.com"),
            )

            every { refreshTokenBlocklistService.isBlocked(refreshToken) } returns true

            then("UnauthorizedException을 던진다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.refresh(refreshToken)
                }

                exception.message shouldBe "차단된 토큰입니다."

                verify(exactly = 1) { refreshTokenBlocklistService.isBlocked(refreshToken) }
            }
        }
    }
})
