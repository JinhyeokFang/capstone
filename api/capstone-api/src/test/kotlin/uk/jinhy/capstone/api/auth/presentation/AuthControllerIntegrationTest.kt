package uk.jinhy.capstone.api.auth.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.api.test.config.IntegrationTest

@IntegrationTest
class AuthControllerIntegrationTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
) : BehaviorSpec({

    isolationMode = io.kotest.core.spec.IsolationMode.InstancePerLeaf

    given("회원가입 API") {
        `when`("유효한 회원가입 요청을 보내면") {
            val request = AuthSignUpRequestDto(
                name = "테스트 유저",
                email = "test@example.com",
                password = "password123",
            )

            then("회원가입에 성공하고 액세스 토큰과 리프레시 토큰을 받는다") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andReturn()

                val setCookieHeader = result.response.getHeader("Set-Cookie")
                setCookieHeader shouldNotBe null
                setCookieHeader!! shouldContain "refreshToken="
                setCookieHeader shouldContain "HttpOnly"

                val user = userRepository.findUserByEmail("test@example.com")
                user shouldNotBe null
                user!!.name shouldBe "테스트 유저"
                user.email shouldBe "test@example.com"
            }
        }

        `when`("이미 존재하는 이메일로 회원가입 요청을 보내면") {
            val request = AuthSignUpRequestDto(
                name = "테스트 유저",
                email = "duplicate@example.com",
                password = "password123",
            )

            mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)

            then("409 Conflict 에러를 반환한다") {
                mockMvc.perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                )
                    .andExpect(status().isConflict)
                    .andExpect(jsonPath("$.success").value(false))
            }
        }

        `when`("유효하지 않은 이메일 형식으로 회원가입 요청을 보내면") {
            val request = AuthSignUpRequestDto(
                name = "테스트 유저",
                email = "invalid-email",
                password = "password123",
            )

            then("400 Bad Request 에러를 반환한다") {
                mockMvc.perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                )
                    .andExpect(status().isBadRequest)
            }
        }

        `when`("비밀번호가 8자 미만일 때") {
            val request = AuthSignUpRequestDto(
                name = "테스트 유저",
                email = "test2@example.com",
                password = "short",
            )

            then("400 Bad Request 에러를 반환한다") {
                mockMvc.perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                )
                    .andExpect(status().isBadRequest)
            }
        }
    }

    given("로그인 API") {
        `when`("유효한 로그인 요청을 보내면") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "로그인 테스트 유저",
                email = "login@example.com",
                password = "password123",
            )

            mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            ).andExpect(status().isOk)

            val loginRequest = AuthLoginRequestDto(
                email = "login@example.com",
                password = "password123",
            )

            then("로그인에 성공하고 액세스 토큰과 리프레시 토큰을 받는다") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)),
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andReturn()

                val setCookieHeader = result.response.getHeader("Set-Cookie")
                setCookieHeader shouldNotBe null
                setCookieHeader!! shouldContain "refreshToken="

                val user = userRepository.findUserByEmail("login@example.com")
                user shouldNotBe null
                user!!.lastLoginAt shouldNotBe null
            }
        }

        `when`("존재하지 않는 이메일로 로그인 요청을 보내면") {
            val request = AuthLoginRequestDto(
                email = "nonexistent@example.com",
                password = "password123",
            )

            then("400 Bad Request 에러를 반환한다") {
                mockMvc.perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.success").value(false))
            }
        }

        `when`("잘못된 비밀번호로 로그인 요청을 보내면") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "비밀번호 테스트 유저",
                email = "wrongpassword@example.com",
                password = "password123",
            )

            mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            ).andExpect(status().isOk)

            val loginRequest = AuthLoginRequestDto(
                email = "wrongpassword@example.com",
                password = "wrongpassword",
            )

            then("400 Bad Request 에러를 반환한다") {
                mockMvc.perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)),
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.success").value(false))
            }
        }
    }

    given("현재 사용자 정보 조회 API") {
        `when`("유효한 액세스 토큰으로 요청하면") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "정보 조회 테스트 유저",
                email = "me@example.com",
                password = "password123",
            )

            val signUpResult = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )
                .andExpect(status().isOk)
                .andReturn()

            val response = objectMapper.readTree(signUpResult.response.contentAsString)
            val accessToken = response.get("data").get("accessToken").asText()

            then("현재 사용자 정보를 반환한다") {
                mockMvc.perform(
                    get("/api/v1/auth/me")
                        .header("Authorization", "Bearer $accessToken"),
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("me@example.com"))
                    .andExpect(jsonPath("$.data.name").value("정보 조회 테스트 유저"))
            }
        }

        `when`("액세스 토큰 없이 요청하면") {
            then("401 Unauthorized 에러를 반환한다") {
                mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized)
            }
        }

        `when`("유효하지 않은 액세스 토큰으로 요청하면") {
            then("401 Unauthorized 에러를 반환한다") {
                mockMvc.perform(
                    get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid-token"),
                )
                    .andExpect(status().isUnauthorized)
            }
        }
    }

    given("로그아웃 API") {
        `when`("유효한 리프레시 토큰으로 로그아웃 요청을 보내면") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "로그아웃 테스트 유저",
                email = "logout@example.com",
                password = "password123",
            )

            val signUpResult = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )
                .andExpect(status().isOk)
                .andReturn()

            val setCookieHeader = signUpResult.response.getHeader("Set-Cookie")!!
            val refreshToken = setCookieHeader.substringAfter("refreshToken=").substringBefore(";")

            then("로그아웃에 성공하고 쿠키가 삭제된다") {
                val result = mockMvc.perform(
                    post("/api/v1/auth/logout")
                        .cookie(
                            org.springframework.mock.web.MockCookie("refreshToken", refreshToken),
                        ),
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn()

                val logoutSetCookie = result.response.getHeader("Set-Cookie")
                logoutSetCookie shouldNotBe null
                logoutSetCookie!! shouldContain "Max-Age=0"
            }
        }

        `when`("리프레시 토큰 없이 로그아웃 요청을 보내면") {
            then("로그아웃에 성공한다") {
                mockMvc.perform(post("/api/v1/auth/logout"))
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
            }
        }
    }

    given("토큰 갱신 API") {
        `when`("유효한 리프레시 토큰으로 요청하면") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "토큰 갱신 테스트 유저",
                email = "refresh@example.com",
                password = "password123",
            )

            val signUpResult = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )
                .andExpect(status().isOk)
                .andReturn()

            val setCookieHeader = signUpResult.response.getHeader("Set-Cookie")!!
            val refreshToken = setCookieHeader.substringAfter("refreshToken=").substringBefore(";")

            then("새로운 액세스 토큰을 받는다") {
                mockMvc.perform(
                    post("/api/v1/auth/refresh")
                        .cookie(
                            org.springframework.mock.web.MockCookie("refreshToken", refreshToken),
                        ),
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
            }
        }

        `when`("리프레시 토큰 없이 요청하면") {
            then("401 Unauthorized 에러를 반환한다") {
                mockMvc.perform(post("/api/v1/auth/refresh"))
                    .andExpect(status().isUnauthorized)
            }
        }

        `when`("유효하지 않은 리프레시 토큰으로 요청하면") {
            then("401 Unauthorized 에러를 반환한다") {
                mockMvc.perform(
                    post("/api/v1/auth/refresh")
                        .cookie(
                            org.springframework.mock.web.MockCookie("refreshToken", "invalid-token"),
                        ),
                )
                    .andExpect(status().isUnauthorized)
            }
        }

        `when`("로그아웃된 리프레시 토큰으로 요청하면") {
            val signUpRequest = AuthSignUpRequestDto(
                name = "차단된 토큰 테스트 유저",
                email = "blockedtoken@example.com",
                password = "password123",
            )

            val signUpResult = mockMvc.perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)),
            )
                .andExpect(status().isOk)
                .andReturn()

            val setCookieHeader = signUpResult.response.getHeader("Set-Cookie")!!
            val refreshToken = setCookieHeader.substringAfter("refreshToken=").substringBefore(";")

            mockMvc.perform(
                post("/api/v1/auth/logout")
                    .cookie(
                        org.springframework.mock.web.MockCookie("refreshToken", refreshToken),
                    ),
            ).andExpect(status().isOk)

            then("401 Unauthorized 에러를 반환한다") {
                mockMvc.perform(
                    post("/api/v1/auth/refresh")
                        .cookie(
                            org.springframework.mock.web.MockCookie("refreshToken", refreshToken),
                        ),
                )
                    .andExpect(status().isUnauthorized)
            }
        }
    }
})
