package uk.jinhy.capstone.api.auth.presentation

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.api.dto.response.LoginResponse
import uk.jinhy.capstone.api.auth.facade.AuthFacade
import uk.jinhy.capstone.util.exception.BadRequestException

@WebMvcTest(
    controllers = [AuthController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        CacheAutoConfiguration::class,
        RedisAutoConfiguration::class,
    ],
    properties = [
        "spring.cache.type=none",
    ],
)
@ComponentScan(
    basePackages = ["uk.jinhy.capstone.api"],
    useDefaultFilters = false,
    includeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [AuthController::class]),
    ],
)
@ImportAutoConfiguration(JacksonAutoConfiguration::class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    @MockkBean private val authFacade: AuthFacade,
) : BehaviorSpec({
    extensions(SpringExtension)

    Given("유효한 로그인 요청이 주어졌을 때") {
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123",
        )
        val response = LoginResponse(accessToken = "test.jwt.token")

        every { authFacade.login(request) } returns response

        When("로그인 API를 호출하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

            Then("200 OK 응답을 받는다") {
                result.andExpect(status().isOk)
            }

            Then("액세스 토큰이 반환된다") {
                result.andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("test.jwt.token"))
            }
        }
    }

    Given("이메일이 누락된 요청이 주어졌을 때") {
        When("로그인 API를 호출하면") {
            val invalidRequest = """{"password": "password123"}"""
            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest),
            )

            Then("400 Bad Request 응답을 받는다") {
                result.andExpect(status().isBadRequest)
            }
        }
    }

    Given("비밀번호가 누락된 요청이 주어졌을 때") {
        When("로그인 API를 호출하면") {
            val invalidRequest = """{"email": "test@example.com"}"""
            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest),
            )

            Then("400 Bad Request 응답을 받는다") {
                result.andExpect(status().isBadRequest)
            }
        }
    }

    Given("잘못된 이메일 형식의 요청이 주어졌을 때") {
        When("로그인 API를 호출하면") {
            val invalidRequest = """{"email": "invalid-email", "password": "password123"}"""
            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest),
            )

            Then("400 Bad Request 응답을 받는다") {
                result.andExpect(status().isBadRequest)
            }
        }
    }

    Given("존재하지 않는 사용자로 로그인을 시도할 때") {
        val request = LoginRequest(
            email = "nonexistent@example.com",
            password = "password123",
        )

        every { authFacade.login(request) } throws BadRequestException(
            message = "사용자를 찾을 수 없습니다.",
            code = "USER_NOT_FOUND",
        )

        When("로그인 API를 호출하면") {
            val result = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

            Then("400 Bad Request 응답을 받는다") {
                result.andExpect(status().isBadRequest)
            }
        }
    }
})
