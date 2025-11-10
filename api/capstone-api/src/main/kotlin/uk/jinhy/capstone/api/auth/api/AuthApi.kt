package uk.jinhy.capstone.api.auth.api

// Temporarily commented out Swagger imports due to dependency issues
// import io.swagger.v3.oas.annotations.Operation
// import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.api.dto.response.LoginResponse
import uk.jinhy.capstone.util.response.ApiResponse

// Temporarily commented out Swagger annotations due to dependency issues
// @Tag(name = "Auth", description = "인증 API")
@RequestMapping("/api/v1/auth")
interface AuthApi {

    // Temporarily commented out Swagger annotations due to dependency issues
    // @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ApiResponse<LoginResponse>
}
