package uk.jinhy.capstone.api.auth.service

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
    private val passwordEncoder: PasswordEncoder,
    @MockkBean private val userRepository: UserRepository,
    @MockkBean private val refreshTokenBlocklistService: RefreshTokenBlocklistService,
) : BehaviorSpec({

    val authService = AuthServiceImpl(
        userRepository = userRepository,
        passwordEncoder = passwordEncoder,
        refreshTokenBlocklistService = refreshTokenBlocklistService,
    )

    Given("유효한 사용자가 존재할 때") {
        val email = "test@example.com"
        val password = "password123"
        val encodedPassword = passwordEncoder.encode(password)
        val now = Instant.now()

        val user = User(
            id = "user-id",
            name = "Test User",
            email = email,
            password = encodedPassword,
            createdAt = now,
            updatedAt = now,
            lastLoginAt = null,
            isActive = true,
        )

        When("올바른 이메일과 비밀번호로 로그인을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns user
            every { userRepository.save(any()) } answers { firstArg() }

            val dto = AuthLoginDto(email = email, password = password)
            val result = authService.login(dto)

            Then("로그인에 성공하고 토큰을 반환한다") {
                result.accessToken shouldNotBe null
                result.refreshToken shouldNotBe null

                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }

        When("잘못된 비밀번호로 로그인을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns user

            val dto = AuthLoginDto(email = email, password = "wrongpassword")

            Then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(dto)
                }
                exception.message shouldBe "비밀번호가 일치하지 않습니다."
                exception.code shouldBe "PASSWORD_MISMATCH"
            }
        }

        When("비활성화된 사용자가 로그인을 시도하면") {
            val inactiveUser = user.copy(isActive = false)
            every { userRepository.findUserByEmail(email) } returns inactiveUser

            val dto = AuthLoginDto(email = email, password = password)

            Then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(dto)
                }
                exception.message shouldBe "비활성화된 사용자입니다."
                exception.code shouldBe "USER_INACTIVE"
            }
        }
    }

    Given("존재하지 않는 사용자로") {
        val email = "nonexistent@example.com"
        val password = "password123"

        When("로그인을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns null

            val dto = AuthLoginDto(email = email, password = password)

            Then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(dto)
                }
                exception.message shouldBe "사용자를 찾을 수 없습니다."
                exception.code shouldBe "USER_NOT_FOUND"
            }
        }
    }

    Given("신규 사용자 정보로") {
        val name = "New User"
        val email = "newuser@example.com"
        val password = "password123"
        val now = Instant.now()

        When("회원가입을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns null
            every { userRepository.save(any()) } answers {
                val user = firstArg<User>()
                user
            }

            val dto = AuthSignUpDto(name = name, email = email, password = password)
            val result = authService.signUp(dto)

            Then("회원가입에 성공하고 토큰을 반환한다") {
                result.accessToken shouldNotBe null
                result.refreshToken shouldNotBe null

                verify(exactly = 1) { userRepository.findUserByEmail(email) }
                verify(exactly = 1) { userRepository.save(any()) }
            }
        }
    }

    Given("이미 존재하는 이메일로") {
        val name = "Existing User"
        val email = "existing@example.com"
        val password = "password123"
        val now = Instant.now()

        val existingUser = User(
            id = "existing-user-id",
            name = "Existing User",
            email = email,
            password = passwordEncoder.encode("oldpassword"),
            createdAt = now,
            updatedAt = now,
            lastLoginAt = null,
            isActive = true,
        )

        When("회원가입을 시도하면") {
            every { userRepository.findUserByEmail(email) } returns existingUser

            val dto = AuthSignUpDto(name = name, email = email, password = password)

            Then("ConflictException이 발생한다") {
                val exception = shouldThrow<ConflictException> {
                    authService.signUp(dto)
                }
                exception.message shouldBe "이미 존재하는 이메일입니다."
                exception.code shouldBe "EMAIL_ALREADY_EXISTS"
            }
        }
    }

    Given("유효한 리프레시 토큰으로") {
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

        When("토큰 갱신을 시도하면") {
            every { refreshTokenBlocklistService.isBlocked(refreshToken) } returns false

            val newAccessToken = authService.refresh(refreshToken)

            Then("새로운 액세스 토큰을 반환한다") {
                newAccessToken shouldNotBe null
                verify(exactly = 1) { refreshTokenBlocklistService.isBlocked(refreshToken) }
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

        When("토큰 갱신을 시도하면") {
            Then("UnauthorizedException이 발생한다") {
                shouldThrow<UnauthorizedException> {
                    authService.refresh(invalidToken)
                }
            }
        }
    }

    Given("블록리스트에 등록된 리프레시 토큰으로") {
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

        When("로그아웃을 시도하면") {
            Then("UnauthorizedException이 발생한다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.logout(accessToken)
                }
                exception.message shouldBe "리프레시 토큰이 아닙니다."
            }
        }

        When("토큰 갱신을 시도하면") {
            Then("UnauthorizedException이 발생한다") {
                val exception = shouldThrow<UnauthorizedException> {
                    authService.refresh(accessToken)
                }
                exception.message shouldBe "리프레시 토큰이 아닙니다."
            }
        }
    }
})
