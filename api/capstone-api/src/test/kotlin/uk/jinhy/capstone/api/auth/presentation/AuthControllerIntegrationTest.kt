package uk.jinhy.capstone.api.auth.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.api.config.IntegrationTest
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.infra.auth.RefreshTokenBlocklistService
import uk.jinhy.capstone.util.jwt.JwtUtil
import java.time.Instant
import jakarta.servlet.http.Cookie

@IntegrationTest
class AuthControllerIntegrationTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenBlocklistService: RefreshTokenBlocklistService,
) : BehaviorSpec({

    afterEach {
        // 각 테스트 후 데이터 정리는 @Transactional에 의해 자동으로 롤백됨
    }

    Given("회원가입 요청이 주어졌을 때") {
        val request = AuthSignUpRequestDto(
            name = "Test User",
            email = "signup@example.com",
            password = "password123",
        )

        When("유효한 회원가입 정보로 요청하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

            Then("201 상태코드와 액세스 토큰, 리프레시 토큰 쿠키를 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(cookie().exists("refreshToken"))
                    .andExpect(cookie().httpOnly("refreshToken", true))
            }
        }

        When("이메일 형식이 잘못된 경우") {
            val invalidRequest = request.copy(email = "invalid-email")

            val result = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )

            Then("400 상태코드를 반환한다") {
                result.andExpect(status().isBadRequest)
            }
        }

        When("비밀번호가 8자 미만인 경우") {
            val invalidRequest = request.copy(password = "short")

            val result = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )

            Then("400 상태코드를 반환한다") {
                result.andExpect(status().isBadRequest)
            }
        }

        When("이름이 비어있는 경우") {
            val invalidRequest = request.copy(name = "")

            val result = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )

            Then("400 상태코드를 반환한다") {
                result.andExpect(status().isBadRequest)
            }
        }
    }

    Given("이미 가입된 사용자가 존재할 때") {
        val now = Instant.now()
        val existingUser = User.create(
            name = "Existing User",
            email = "existing@example.com",
            password = passwordEncoder.encode("password123"),
            createdAt = now,
            updatedAt = now,
        )
        userRepository.save(existingUser)

        When("동일한 이메일로 회원가입을 시도하면") {
            val request = AuthSignUpRequestDto(
                name = "Another User",
                email = existingUser.email,
                password = "password123",
            )

            val result = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

            Then("409 상태코드를 반환한다") {
                result
                    .andExpect(status().isConflict)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"))
            }
        }
    }

    Given("등록된 사용자 정보로") {
        val email = "login@example.com"
        val password = "password123"
        val now = Instant.now()

        val user = User.create(
            name = "Login User",
            email = email,
            password = passwordEncoder.encode(password),
            createdAt = now,
            updatedAt = now,
        )
        userRepository.save(user)

        When("올바른 이메일과 비밀번호로 로그인하면") {
            val request = AuthLoginRequestDto(
                email = email,
                password = password,
            )

            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

            Then("200 상태코드와 액세스 토큰, 리프레시 토큰 쿠키를 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(cookie().exists("refreshToken"))
                    .andExpect(cookie().httpOnly("refreshToken", true))
            }
        }

        When("잘못된 비밀번호로 로그인하면") {
            val request = AuthLoginRequestDto(
                email = email,
                password = "wrongpassword",
            )

            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

            Then("400 상태코드를 반환한다") {
                result
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PASSWORD_MISMATCH"))
            }
        }

        When("존재하지 않는 이메일로 로그인하면") {
            val request = AuthLoginRequestDto(
                email = "nonexistent@example.com",
                password = password,
            )

            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

            Then("400 상태코드를 반환한다") {
                result
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
            }
        }
    }

    Given("인증된 사용자로") {
        val email = "me@example.com"
        val password = "password123"
        val now = Instant.now()

        val user = User.create(
            name = "Me User",
            email = email,
            password = passwordEncoder.encode(password),
            createdAt = now,
            updatedAt = now,
        )
        val savedUser = userRepository.save(user)

        val accessToken = JwtUtil.generateAccessToken(
            subject = savedUser.id,
            claims = mapOf("email" to savedUser.email),
        )

        When("현재 사용자 정보를 조회하면") {
            val result = mockMvc.perform(
                get("/api/v1/auth/me")
                    .header("Authorization", "Bearer $accessToken"),
            )

            Then("200 상태코드와 사용자 정보를 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(savedUser.id))
                    .andExpect(jsonPath("$.data.name").value(savedUser.name))
                    .andExpect(jsonPath("$.data.email").value(savedUser.email))
            }
        }
    }

    Given("인증 토큰 없이") {
        When("현재 사용자 정보를 조회하면") {
            val result = mockMvc.perform(get("/api/v1/auth/me"))

            Then("401 상태코드를 반환한다") {
                result.andExpect(status().isUnauthorized)
            }
        }
    }

    Given("유효한 리프레시 토큰으로") {
        val email = "refresh@example.com"
        val password = "password123"
        val now = Instant.now()

        val user = User.create(
            name = "Refresh User",
            email = email,
            password = passwordEncoder.encode(password),
            createdAt = now,
            updatedAt = now,
        )
        val savedUser = userRepository.save(user)

        val refreshToken = JwtUtil.generateRefreshToken(
            subject = savedUser.id,
            claims = mapOf("email" to savedUser.email),
        )

        When("토큰 갱신을 요청하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/refresh")
                    .cookie(Cookie("refreshToken", refreshToken)),
            )

            Then("200 상태코드와 새로운 액세스 토큰을 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
            }
        }

        When("로그아웃을 요청하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/logout")
                    .cookie(Cookie("refreshToken", refreshToken)),
            )

            Then("200 상태코드와 함께 리프레시 토큰 쿠키를 삭제한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(cookie().maxAge("refreshToken", 0))
            }
        }
    }

    Given("리프레시 토큰 없이") {
        When("토큰 갱신을 요청하면") {
            val result = mockMvc.perform(post("/api/v1/auth/refresh"))

            Then("401 상태코드를 반환한다") {
                result
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.success").value(false))
            }
        }

        When("로그아웃을 요청하면") {
            val result = mockMvc.perform(post("/api/v1/auth/logout"))

            Then("200 상태코드를 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
            }
        }
    }

    Given("비활성화된 사용자로") {
        val email = "inactive@example.com"
        val password = "password123"
        val now = Instant.now()

        val user = User(
            id = "inactive-user-id",
            name = "Inactive User",
            email = email,
            password = passwordEncoder.encode(password),
            createdAt = now,
            updatedAt = now,
            lastLoginAt = null,
            isActive = false,
        )
        userRepository.save(user)

        When("로그인을 시도하면") {
            val request = AuthLoginRequestDto(
                email = email,
                password = password,
            )

            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

            Then("400 상태코드를 반환한다") {
                result
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("USER_INACTIVE"))
            }
        }
    }

    Given("블록리스트에 등록된 리프레시 토큰으로") {
        val email = "blocked@example.com"
        val password = "password123"
        val now = Instant.now()

        val user = User.create(
            name = "Blocked User",
            email = email,
            password = passwordEncoder.encode(password),
            createdAt = now,
            updatedAt = now,
        )
        val savedUser = userRepository.save(user)

        val refreshToken = JwtUtil.generateRefreshToken(
            subject = savedUser.id,
            claims = mapOf("email" to savedUser.email),
        )

        // 토큰을 블록리스트에 추가
        val expirationMillis = JwtUtil.getExpirationMillis(refreshToken)
        refreshTokenBlocklistService.addToBlocklist(refreshToken, expirationMillis)

        When("토큰 갱신을 요청하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/refresh")
                    .cookie(Cookie("refreshToken", refreshToken)),
            )

            Then("401 상태코드를 반환한다") {
                result
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.success").value(false))
            }
        }
    }
})
