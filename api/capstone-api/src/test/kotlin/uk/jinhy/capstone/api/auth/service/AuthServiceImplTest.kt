package uk.jinhy.capstone.api.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.util.JwtUtil
import uk.jinhy.capstone.util.exception.BadRequestException
import java.time.Instant

class AuthServiceImplTest : BehaviorSpec({

    val userRepository = mockk<UserRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val jwtUtil = mockk<JwtUtil>()
    val authService = AuthServiceImpl(userRepository, passwordEncoder, jwtUtil)

    Given("활성화된 사용자가 존재할 때") {
        val email = "test@example.com"
        val password = "password123"
        val encodedPassword = "\$2a\$10\$encodedPassword"
        val user = User(
            id = 1L,
            name = "Test User",
            email = email,
            password = encodedPassword,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = true,
        )

        every { userRepository.findUserByEmail(email) } returns user
        every { passwordEncoder.matches(password, encodedPassword) } returns true
        every { jwtUtil.generateToken(any(), any()) } returns "test.jwt.token"
        every { userRepository.saveUser(any()) } answers { firstArg() }

        When("올바른 비밀번호로 로그인을 시도하면") {
            val request = LoginRequest(email = email, password = password)
            val response = authService.login(request)

            Then("액세스 토큰이 반환된다") {
                response.accessToken shouldBe "test.jwt.token"
            }

            Then("사용자 정보가 업데이트된다") {
                verify(exactly = 1) {
                    userRepository.saveUser(match { it.lastLoginAt != null })
                }
            }

            Then("JWT 토큰이 생성된다") {
                verify(exactly = 1) {
                    jwtUtil.generateToken(
                        subject = "1",
                        claims = mapOf("email" to email),
                    )
                }
            }
        }

        When("잘못된 비밀번호로 로그인을 시도하면") {
            every { passwordEncoder.matches("wrongPassword", encodedPassword) } returns false

            val request = LoginRequest(email = email, password = "wrongPassword")

            Then("PASSWORD_MISMATCH 예외가 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(request)
                }
                exception.code shouldBe "PASSWORD_MISMATCH"
                exception.message shouldBe "비밀번호가 일치하지 않습니다."
            }
        }
    }

    Given("비활성화된 사용자가 존재할 때") {
        val email = "inactive@example.com"
        val password = "password123"
        val encodedPassword = "\$2a\$10\$encodedPassword"
        val inactiveUser = User(
            id = 2L,
            name = "Inactive User",
            email = email,
            password = encodedPassword,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = false,
        )

        every { userRepository.findUserByEmail(email) } returns inactiveUser
        every { passwordEncoder.matches(password, encodedPassword) } returns true

        When("로그인을 시도하면") {
            val request = LoginRequest(email = email, password = password)

            Then("USER_INACTIVE 예외가 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(request)
                }
                exception.code shouldBe "USER_INACTIVE"
                exception.message shouldBe "비활성화된 사용자입니다."
            }
        }
    }

    Given("존재하지 않는 사용자 이메일로") {
        val email = "nonexistent@example.com"
        every { userRepository.findUserByEmail(email) } returns null

        When("로그인을 시도하면") {
            val request = LoginRequest(email = email, password = "password123")

            Then("USER_NOT_FOUND 예외가 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(request)
                }
                exception.code shouldBe "USER_NOT_FOUND"
                exception.message shouldBe "사용자를 찾을 수 없습니다."
            }
        }
    }

    Given("비밀번호가 null인 사용자가 존재할 때") {
        val email = "nopassword@example.com"
        val user = User(
            id = 3L,
            name = "No Password User",
            email = email,
            password = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = true,
        )

        every { userRepository.findUserByEmail(email) } returns user

        When("로그인을 시도하면") {
            val request = LoginRequest(email = email, password = "password123")

            Then("PASSWORD_MISMATCH 예외가 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    authService.login(request)
                }
                exception.code shouldBe "PASSWORD_MISMATCH"
                exception.message shouldBe "비밀번호가 일치하지 않습니다."
            }
        }
    }
})
