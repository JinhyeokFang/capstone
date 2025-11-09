package uk.jinhy.capstone.api.auth

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.api.auth.repository.entity.UserEntity
import uk.jinhy.capstone.util.response.ApiResponse
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest(
    @LocalServerPort private val port: Int,
    private val restTemplate: TestRestTemplate,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : BehaviorSpec({
    extensions(SpringExtension)

    fun baseUrl() = "http://localhost:$port/api/v1/auth"

    beforeEach {
        userRepository.deleteAll()
    }

    Given("활성화된 사용자가 등록되어 있을 때") {
        val email = "test@example.com"
        val rawPassword = "password123"
        val encodedPassword = passwordEncoder.encode(rawPassword)

        userRepository.save(
            UserEntity(
                name = "Test User",
                email = email,
                password = encodedPassword,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                isActive = true,
            ),
        )

        When("올바른 인증 정보로 로그인을 시도하면") {
            val request = LoginRequest(email = email, password = rawPassword)
            val response = restTemplate.postForEntity(
                "${baseUrl()}/login",
                request,
                ApiResponse::class.java,
            )

            Then("200 OK 응답을 받는다") {
                response.statusCode shouldBe HttpStatus.OK
            }

            Then("성공 응답이 반환된다") {
                val body = response.body
                body shouldNotBe null
                body?.success shouldBe true
            }

            Then("액세스 토큰이 포함되어 있다") {
                // Jackson이 LinkedHashMap으로 변환하므로 타입 캐스팅 필요
                @Suppress("UNCHECKED_CAST")
                val data = response.body?.data as? Map<String, Any>
                val accessToken = data?.get("accessToken") as? String
                accessToken shouldNotBe null
            }

            Then("사용자의 마지막 로그인 시간이 업데이트된다") {
                val user = userRepository.findUserByEmail(email)
                user shouldNotBe null
                user?.lastLoginAt shouldNotBe null
            }
        }

        When("잘못된 비밀번호로 로그인을 시도하면") {
            val request = LoginRequest(email = email, password = "wrongPassword")
            val response = restTemplate.postForEntity(
                "${baseUrl()}/login",
                request,
                ApiResponse::class.java,
            )

            Then("400 Bad Request 응답을 받는다") {
                response.statusCode shouldBe HttpStatus.BAD_REQUEST
            }

            Then("실패 응답이 반환된다") {
                val body = response.body
                body shouldNotBe null
                body?.success shouldBe false
            }
        }
    }

    Given("비활성화된 사용자가 등록되어 있을 때") {
        val email = "inactive@example.com"
        val rawPassword = "password123"
        val encodedPassword = passwordEncoder.encode(rawPassword)

        userRepository.save(
            UserEntity(
                name = "Inactive User",
                email = email,
                password = encodedPassword,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                isActive = false,
            ),
        )

        When("로그인을 시도하면") {
            val request = LoginRequest(email = email, password = rawPassword)
            val response = restTemplate.postForEntity(
                "${baseUrl()}/login",
                request,
                ApiResponse::class.java,
            )

            Then("400 Bad Request 응답을 받는다") {
                response.statusCode shouldBe HttpStatus.BAD_REQUEST
            }

            Then("실패 응답이 반환된다") {
                val body = response.body
                body shouldNotBe null
                body?.success shouldBe false
            }
        }
    }

    Given("존재하지 않는 사용자 이메일로") {
        When("로그인을 시도하면") {
            val request = LoginRequest(
                email = "nonexistent@example.com",
                password = "password123",
            )
            val response = restTemplate.postForEntity(
                "${baseUrl()}/login",
                request,
                ApiResponse::class.java,
            )

            Then("400 Bad Request 응답을 받는다") {
                response.statusCode shouldBe HttpStatus.BAD_REQUEST
            }

            Then("실패 응답이 반환된다") {
                val body = response.body
                body shouldNotBe null
                body?.success shouldBe false
            }
        }
    }

    Given("유효하지 않은 요청 데이터로") {
        When("이메일이 누락된 요청을 보내면") {
            val invalidRequest = mapOf("password" to "password123")
            val response = restTemplate.postForEntity(
                "${baseUrl()}/login",
                invalidRequest,
                ApiResponse::class.java,
            )

            Then("400 Bad Request 응답을 받는다") {
                response.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }

        When("잘못된 이메일 형식으로 요청을 보내면") {
            val invalidRequest = LoginRequest(
                email = "invalid-email-format",
                password = "password123",
            )
            val response = restTemplate.postForEntity(
                "${baseUrl()}/login",
                invalidRequest,
                ApiResponse::class.java,
            )

            Then("400 Bad Request 응답을 받는다") {
                response.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }
    }
})
