package uk.jinhy.capstone.api.auth.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.config.IntegrationTest

@IntegrationTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) : BehaviorSpec({
    extension(SpringExtension)

    Given("회원가입 요청") {
        val signUpRequest = AuthSignUpRequestDto(
            name = "테스트 사용자",
            email = "test@example.com",
            password = "password123!",
        )

        When("유효한 정보로 회원가입하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )

            Then("성공 응답과 액세스 토큰을 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
            }
        }

        When("이미 가입된 이메일로 회원가입하면") {
            mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )

            val result = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )

            Then("에러 응답을 반환한다") {
                result
                    .andExpect(status().is4xxClientError)
            }
        }
    }

    Given("로그인 요청") {
        val signUpRequest = AuthSignUpRequestDto(
            name = "로그인 테스트",
            email = "login@example.com",
            password = "password123!",
        )

        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)),
        )

        val loginRequest = AuthLoginRequestDto(
            email = "login@example.com",
            password = "password123!",
        )

        When("올바른 계정 정보로 로그인하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)),
            )

            Then("성공 응답과 토큰을 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
            }
        }

        When("잘못된 비밀번호로 로그인하면") {
            val wrongPasswordRequest = AuthLoginRequestDto(
                email = "login@example.com",
                password = "wrongpassword",
            )

            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongPasswordRequest)),
            )

            Then("에러 응답을 반환한다") {
                result
                    .andExpect(status().is4xxClientError)
            }
        }
    }

    Given("인증된 사용자의 정보 조회 요청") {
        val signUpRequest = AuthSignUpRequestDto(
            name = "정보조회 테스트",
            email = "me@example.com",
            password = "password123!",
        )

        val signUpResponse = mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)),
        ).andReturn()

        val responseBody = signUpResponse.response.contentAsString
        val responseMap = objectMapper.readValue(responseBody, Map::class.java)
        val accessToken = (responseMap["data"] as Map<*, *>)["accessToken"] as String

        When("유효한 액세스 토큰으로 요청하면") {
            val result = mockMvc.perform(
                get("/api/v1/auth/me")
                    .header("Authorization", "Bearer $accessToken"),
            )

            Then("사용자 정보를 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("me@example.com"))
                    .andExpect(jsonPath("$.data.name").value("정보조회 테스트"))
            }
        }

        When("액세스 토큰 없이 요청하면") {
            val result = mockMvc.perform(
                get("/api/v1/auth/me"),
            )

            Then("인증 에러를 반환한다") {
                result
                    .andExpect(status().is4xxClientError)
            }
        }
    }

    Given("토큰 갱신 요청") {
        val signUpRequest = AuthSignUpRequestDto(
            name = "토큰갱신 테스트",
            email = "refresh@example.com",
            password = "password123!",
        )

        val signUpResponse = mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)),
        ).andReturn()

        val cookies = signUpResponse.response.cookies
        val refreshTokenCookie = cookies.find { it.name == "refreshToken" }

        When("유효한 리프레시 토큰으로 요청하면") {
            refreshTokenCookie shouldNotBe null

            val result = mockMvc.perform(
                post("/api/v1/auth/refresh")
                    .cookie(refreshTokenCookie!!),
            )

            Then("새로운 액세스 토큰을 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
            }
        }

        When("리프레시 토큰 없이 요청하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/refresh"),
            )

            Then("에러 응답을 반환한다") {
                result
                    .andExpect(status().is4xxClientError)
            }
        }
    }

    Given("로그아웃 요청") {
        val signUpRequest = AuthSignUpRequestDto(
            name = "로그아웃 테스트",
            email = "logout@example.com",
            password = "password123!",
        )

        val signUpResponse = mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)),
        ).andReturn()

        val cookies = signUpResponse.response.cookies
        val refreshTokenCookie = cookies.find { it.name == "refreshToken" }

        When("유효한 리프레시 토큰으로 로그아웃하면") {
            refreshTokenCookie shouldNotBe null

            val result = mockMvc.perform(
                post("/api/v1/auth/logout")
                    .cookie(refreshTokenCookie!!),
            )

            Then("성공 응답을 반환하고 쿠키를 삭제한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))

                val responseCookies = result.andReturn().response.cookies
                val logoutCookie = responseCookies.find { it.name == "refreshToken" }
                logoutCookie shouldNotBe null
                logoutCookie?.maxAge shouldBe 0
            }
        }

        When("리프레시 토큰 없이 로그아웃하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/logout"),
            )

            Then("성공 응답을 반환한다") {
                result
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
            }
        }
    }
})
