package uk.jinhy.capstone.api.auth.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.api.common.IntegrationTestBase
import uk.jinhy.capstone.infra.auth.RefreshTokenBlocklistService

@AutoConfigureMockMvc
class AuthControllerIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var refreshTokenBlocklistService: RefreshTokenBlocklistService

    init {
        beforeEach {
            // 각 테스트 전에 데이터 정리는 자동으로 됨 (testcontainer rollback)
        }

        Given("새로운 사용자 정보가 주어졌을 때") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "Test User",
                email = "signup-test-${System.currentTimeMillis()}@example.com",
                password = "password123",
            )

            When("회원가입을 수행하면") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)),
                )

                Then("회원가입이 성공한다") {
                    result.andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.accessToken").exists())
                }

                Then("리프레시 토큰 쿠키가 설정된다") {
                    val response = result.andReturn().response
                    val setCookieHeader = response.getHeader("Set-Cookie")
                    setCookieHeader shouldNotBe null
                    setCookieHeader!! shouldContain "refreshToken="
                    setCookieHeader shouldContain "HttpOnly"
                }

                Then("사용자가 데이터베이스에 저장된다") {
                    val user = userRepository.findUserByEmail(signUpRequest.email)
                    user shouldNotBe null
                    user!!.name shouldBe signUpRequest.name
                    user.email shouldBe signUpRequest.email
                    user.isActive shouldBe true
                }
            }
        }

        Given("이미 존재하는 이메일로") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "Test User",
                email = "existing-${System.currentTimeMillis()}@example.com",
                password = "password123",
            )

            // 먼저 회원가입
            mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )

            When("회원가입을 시도하면") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)),
                )

                Then("ConflictException이 발생한다") {
                    result.andExpect(status().isConflict)
                        .andExpect(jsonPath("$.success").value(false))
                }
            }
        }

        Given("등록된 사용자의 올바른 자격증명이 주어졌을 때") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "Login Test User",
                email = "logintest-${System.currentTimeMillis()}@example.com",
                password = "password123",
            )

            // 먼저 회원가입
            mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )

            val loginRequest = AuthLoginRequestDto(
                email = signUpRequest.email,
                password = signUpRequest.password,
            )

            When("로그인을 수행하면") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)),
                )

                Then("로그인이 성공한다") {
                    result.andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.accessToken").exists())
                }

                Then("리프레시 토큰 쿠키가 설정된다") {
                    val response = result.andReturn().response
                    val setCookieHeader = response.getHeader("Set-Cookie")
                    setCookieHeader shouldNotBe null
                    setCookieHeader!! shouldContain "refreshToken="
                }
            }
        }

        Given("잘못된 비밀번호로") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "Wrong Password User",
                email = "wrongpassword-${System.currentTimeMillis()}@example.com",
                password = "password123",
            )

            // 먼저 회원가입
            mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )

            val loginRequest = AuthLoginRequestDto(
                email = signUpRequest.email,
                password = "wrongPassword",
            )

            When("로그인을 시도하면") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)),
                )

                Then("BadRequestException이 발생한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(jsonPath("$.success").value(false))
                }
            }
        }

        Given("로그인한 사용자가") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "Me Test User",
                email = "metest-${System.currentTimeMillis()}@example.com",
                password = "password123",
            )

            // 먼저 회원가입
            val signUpResult = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            ).andReturn()

            val signUpResponse = objectMapper.readTree(signUpResult.response.contentAsString)
            val accessToken = signUpResponse.get("data").get("accessToken").asText()

            When("사용자 정보를 조회하면") {
                val result = mockMvc.perform(
                    get("/api/v1/auth/me")
                        .header("Authorization", "Bearer $accessToken"),
                )

                Then("사용자 정보가 반환된다") {
                    result.andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.name").value(signUpRequest.name))
                        .andExpect(jsonPath("$.data.email").value(signUpRequest.email))
                }
            }
        }

        Given("유효한 리프레시 토큰이 주어졌을 때") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "Refresh Test User",
                email = "refreshtest-${System.currentTimeMillis()}@example.com",
                password = "password123",
            )

            // 먼저 회원가입
            val signUpResult = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            ).andReturn()

            val setCookieHeader = signUpResult.response.getHeader("Set-Cookie")!!
            val refreshToken = setCookieHeader.split(";")[0].split("=")[1]

            When("토큰 갱신을 수행하면") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/refresh")
                        .cookie(jakarta.servlet.http.Cookie("refreshToken", refreshToken)),
                )

                Then("새로운 액세스 토큰이 반환된다") {
                    result.andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.accessToken").exists())
                }
            }
        }

        Given("로그인한 사용자가 리프레시 토큰을 가지고") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "Logout Test User",
                email = "logouttest-${System.currentTimeMillis()}@example.com",
                password = "password123",
            )

            // 먼저 회원가입
            val signUpResult = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            ).andReturn()

            val setCookieHeader = signUpResult.response.getHeader("Set-Cookie")!!
            val refreshToken = setCookieHeader.split(";")[0].split("=")[1]

            When("로그아웃을 수행하면") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/logout")
                        .cookie(jakarta.servlet.http.Cookie("refreshToken", refreshToken)),
                )

                Then("로그아웃이 성공한다") {
                    result.andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                }

                Then("리프레시 토큰이 블록리스트에 추가된다") {
                    val isBlocked = refreshTokenBlocklistService.isBlocked(refreshToken)
                    isBlocked shouldBe true
                }

                Then("리프레시 토큰 쿠키가 삭제된다") {
                    val response = result.andReturn().response
                    val newSetCookieHeader = response.getHeader("Set-Cookie")
                    newSetCookieHeader shouldNotBe null
                    newSetCookieHeader!! shouldContain "Max-Age=0"
                }
            }
        }

        Given("로그아웃한 사용자의 리프레시 토큰으로") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "Blocklist Test User",
                email = "blocklisttest-${System.currentTimeMillis()}@example.com",
                password = "password123",
            )

            // 먼저 회원가입
            val signUpResult = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            ).andReturn()

            val setCookieHeader = signUpResult.response.getHeader("Set-Cookie")!!
            val refreshToken = setCookieHeader.split(";")[0].split("=")[1]

            // 로그아웃
            mockMvc.perform(
                post("/api/v1/auth/logout")
                    .cookie(jakarta.servlet.http.Cookie("refreshToken", refreshToken)),
            )

            When("토큰 갱신을 시도하면") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/refresh")
                        .cookie(jakarta.servlet.http.Cookie("refreshToken", refreshToken)),
                )

                Then("UnauthorizedException이 발생한다") {
                    result.andExpect(status().isUnauthorized)
                        .andExpect(jsonPath("$.success").value(false))
                }
            }
        }
    }
}
