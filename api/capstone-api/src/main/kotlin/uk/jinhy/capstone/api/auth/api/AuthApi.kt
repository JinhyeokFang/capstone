package uk.jinhy.capstone.api.auth.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthLoginResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthMeResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthSignUpResponseDto
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.infra.security.annotation.CurrentUser
import uk.jinhy.capstone.util.response.ApiResponse

@Tag(name = "Auth", description = "인증 API")
@RequestMapping("/api/v1/auth")
interface AuthApi {

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: AuthLoginRequestDto,
    ): ApiResponse<AuthLoginResponseDto>

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: AuthSignUpRequestDto,
    ): ApiResponse<AuthSignUpResponseDto>

    @Operation(summary = "현재 사용자 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    fun me(@CurrentUser user: User): ApiResponse<AuthMeResponseDto>
}
