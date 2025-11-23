package uk.jinhy.capstone.api.auth.presentation

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.support.IntegrationTestSupport
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.util.jwt.JwtUtil
import java.time.Instant
import jakarta.servlet.http.Cookie

class AuthControllerIntegrationTest : IntegrationTestSupport() {

    private lateinit var passwordEncoder: PasswordEncoder

    override fun beforeSpec(spec: io.kotest.core.spec.Spec) {
        super.beforeSpec(spec)
        passwordEncoder = applicationContext.getBean(PasswordEncoder::class.java)
    }

    init {
        Given("회원가입 요청이 주어졌을 때") {
            val request = AuthSignUpRequestDto(
                name = "홍길동",
                email = "test@example.com",
                password = "password123"
            )

            When("유효한 정보로 회원가입하면") {
                Then("성공 응답과 함께 액세스 토큰 및 리프레시 토큰을 반환한다") {
                    val result = mockMvc.perform(
                        post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.accessToken").exists())
                        .andExpect(cookie().exists("refreshToken"))
                        .andReturn()

                    val responseBody = objectMapper.readTree(result.response.contentAsString)
                    val accessToken = responseBody.get("data").get("accessToken").asText()
                    accessToken.shouldNotBeBlank()

                    val cookies = result.response.cookies
                    val refreshTokenCookie = cookies.find { it.name == "refreshToken" }
                    refreshTokenCookie shouldNotBe null
                    refreshTokenCookie!!.value.shouldNotBeBlank()
                    refreshTokenCookie.isHttpOnly shouldBe true
                }
            }

            When("이미 존재하는 이메일로 회원가입하면") {
                // 먼저 사용자 생성
                val now = Instant.now()
                val existingUser = User.create(
                    name = "Existing User",
                    email = request.email,
                    password = passwordEncoder.encode("password123"),
                    createdAt = now,
                    updatedAt = now
                )
                userRepository.save(existingUser)

                Then("ConflictException을 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isConflict)
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"))
                }
            }

            When("유효하지 않은 이메일 형식으로 회원가입하면") {
                val invalidRequest = AuthSignUpRequestDto(
                    name = "Test User",
                    email = "invalid-email",
                    password = "password123"
                )

                Then("유효성 검사 에러를 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest))
                    )
                        .andExpect(status().isBadRequest)
                }
            }

            When("비밀번호가 8자 미만이면") {
                val shortPasswordRequest = AuthSignUpRequestDto(
                    name = "Test User",
                    email = "test2@example.com",
                    password = "short"
                )

                Then("유효성 검사 에러를 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(shortPasswordRequest))
                    )
                        .andExpect(status().isBadRequest)
                }
            }
        }

        Given("로그인 요청이 주어졌을 때") {
            val email = "login@example.com"
            val password = "password123"
            val now = Instant.now()

            // 사용자 생성
            val user = User.create(
                name = "Login User",
                email = email,
                password = passwordEncoder.encode(password),
                createdAt = now,
                updatedAt = now
            )
            userRepository.save(user)

            val request = AuthLoginRequestDto(
                email = email,
                password = password
            )

            When("올바른 이메일과 비밀번호로 로그인하면") {
                Then("성공 응답과 함께 액세스 토큰 및 리프레시 토큰을 반환한다") {
                    val result = mockMvc.perform(
                        post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.accessToken").exists())
                        .andExpect(cookie().exists("refreshToken"))
                        .andReturn()

                    val responseBody = objectMapper.readTree(result.response.contentAsString)
                    val accessToken = responseBody.get("data").get("accessToken").asText()
                    accessToken.shouldNotBeBlank()

                    // 사용자의 lastLoginAt이 업데이트되었는지 확인
                    val updatedUser = userRepository.findUserByEmail(email)
                    updatedUser shouldNotBe null
                    updatedUser!!.lastLoginAt shouldNotBe null
                }
            }

            When("잘못된 비밀번호로 로그인하면") {
                val wrongPasswordRequest = AuthLoginRequestDto(
                    email = email,
                    password = "wrongpassword"
                )

                Then("BadRequestException을 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongPasswordRequest))
                    )
                        .andExpect(status().isBadRequest)
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("PASSWORD_MISMATCH"))
                }
            }

            When("존재하지 않는 이메일로 로그인하면") {
                val nonExistentRequest = AuthLoginRequestDto(
                    email = "nonexistent@example.com",
                    password = password
                )

                Then("BadRequestException을 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonExistentRequest))
                    )
                        .andExpect(status().isBadRequest)
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
                }
            }
        }

        Given("인증된 사용자가 주어졌을 때") {
            val email = "authenticated@example.com"
            val password = "password123"
            val now = Instant.now()

            val user = User.create(
                name = "Authenticated User",
                email = email,
                password = passwordEncoder.encode(password),
                createdAt = now,
                updatedAt = now
            )
            val savedUser = userRepository.save(user)

            val accessToken = JwtUtil.generateAccessToken(
                subject = savedUser.id,
                claims = mapOf("email" to savedUser.email)
            )

            When("현재 사용자 정보를 조회하면") {
                Then("사용자 정보를 반환한다") {
                    mockMvc.perform(
                        get("/api/v1/auth/me")
                            .header("Authorization", "Bearer $accessToken")
                    )
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.id").value(savedUser.id))
                        .andExpect(jsonPath("$.data.name").value(savedUser.name))
                        .andExpect(jsonPath("$.data.email").value(savedUser.email))
                }
            }

            When("액세스 토큰 없이 조회하면") {
                Then("UnauthorizedException을 반환한다") {
                    mockMvc.perform(
                        get("/api/v1/auth/me")
                    )
                        .andExpect(status().isUnauthorized)
                }
            }

            When("유효하지 않은 토큰으로 조회하면") {
                Then("UnauthorizedException을 반환한다") {
                    mockMvc.perform(
                        get("/api/v1/auth/me")
                            .header("Authorization", "Bearer invalid.token.here")
                    )
                        .andExpect(status().isUnauthorized)
                }
            }
        }

        Given("로그아웃 요청이 주어졌을 때") {
            val email = "logout@example.com"
            val password = "password123"
            val now = Instant.now()

            val user = User.create(
                name = "Logout User",
                email = email,
                password = passwordEncoder.encode(password),
                createdAt = now,
                updatedAt = now
            )
            val savedUser = userRepository.save(user)

            val refreshToken = JwtUtil.generateRefreshToken(
                subject = savedUser.id,
                claims = mapOf("email" to savedUser.email)
            )

            When("유효한 리프레시 토큰으로 로그아웃하면") {
                Then("리프레시 토큰을 블록리스트에 추가하고 쿠키를 삭제한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/logout")
                            .cookie(Cookie("refreshToken", refreshToken))
                    )
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(cookie().maxAge("refreshToken", 0))

                    // 토큰이 블록리스트에 추가되었는지 확인
                    val isBlocked = refreshTokenBlocklistService.isBlocked(refreshToken)
                    isBlocked shouldBe true
                }
            }

            When("리프레시 토큰 없이 로그아웃하면") {
                Then("성공 응답을 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/logout")
                    )
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(cookie().maxAge("refreshToken", 0))
                }
            }
        }

        Given("토큰 갱신 요청이 주어졌을 때") {
            val email = "refresh@example.com"
            val password = "password123"
            val now = Instant.now()

            val user = User.create(
                name = "Refresh User",
                email = email,
                password = passwordEncoder.encode(password),
                createdAt = now,
                updatedAt = now
            )
            val savedUser = userRepository.save(user)

            val refreshToken = JwtUtil.generateRefreshToken(
                subject = savedUser.id,
                claims = mapOf("email" to savedUser.email)
            )

            When("유효한 리프레시 토큰으로 갱신하면") {
                Then("새로운 액세스 토큰을 발급한다") {
                    val result = mockMvc.perform(
                        post("/api/v1/auth/refresh")
                            .cookie(Cookie("refreshToken", refreshToken))
                    )
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.accessToken").exists())
                        .andReturn()

                    val responseBody = objectMapper.readTree(result.response.contentAsString)
                    val newAccessToken = responseBody.get("data").get("accessToken").asText()
                    newAccessToken.shouldNotBeBlank()

                    // 새로운 액세스 토큰의 유효성 검사
                    JwtUtil.validateToken(newAccessToken) shouldBe true
                    JwtUtil.extractSubject(newAccessToken) shouldBe savedUser.id
                }
            }

            When("리프레시 토큰 없이 갱신하면") {
                Then("UnauthorizedException을 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/refresh")
                    )
                        .andExpect(status().isUnauthorized)
                        .andExpect(jsonPath("$.success").value(false))
                }
            }

            When("유효하지 않은 리프레시 토큰으로 갱신하면") {
                Then("UnauthorizedException을 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/refresh")
                            .cookie(Cookie("refreshToken", "invalid.token.here"))
                    )
                        .andExpect(status().isUnauthorized)
                        .andExpect(jsonPath("$.success").value(false))
                }
            }

            When("블록리스트에 있는 토큰으로 갱신하면") {
                // 토큰을 블록리스트에 추가
                refreshTokenBlocklistService.addToBlocklist(refreshToken, 604800000)

                Then("UnauthorizedException을 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/refresh")
                            .cookie(Cookie("refreshToken", refreshToken))
                    )
                        .andExpect(status().isUnauthorized)
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.message").value("차단된 토큰입니다."))
                }
            }

            When("액세스 토큰으로 갱신하면") {
                val accessToken = JwtUtil.generateAccessToken(
                    subject = savedUser.id,
                    claims = mapOf("email" to savedUser.email)
                )

                Then("UnauthorizedException을 반환한다") {
                    mockMvc.perform(
                        post("/api/v1/auth/refresh")
                            .cookie(Cookie("refreshToken", accessToken))
                    )
                        .andExpect(status().isUnauthorized)
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.message").value("리프레시 토큰이 아닙니다."))
                }
            }
        }
    }
}
