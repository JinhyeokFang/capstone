package uk.jinhy.capstone.api.auth.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthLoginResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthMeResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthRefreshResponseDto
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
        response: HttpServletResponse,
    ): ApiResponse<AuthLoginResponseDto>

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: AuthSignUpRequestDto,
        response: HttpServletResponse,
    ): ApiResponse<AuthSignUpResponseDto>

    @Operation(summary = "현재 사용자 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    fun me(@CurrentUser user: User): ApiResponse<AuthMeResponseDto>

    @Operation(summary = "로그아웃", description = "로그아웃하고 리프레시 토큰을 블록리스트에 추가합니다.")
    @PostMapping("/logout")
    fun logout(
        @CookieValue(name = "refreshToken", required = false) refreshToken: String?,
        response: HttpServletResponse,
    ): ApiResponse<Unit>

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.")
    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = "refreshToken", required = false) refreshToken: String?,
    ): ApiResponse<AuthRefreshResponseDto>

    @Operation(summary = "Sentry 에러 추적 테스트", description = "Sentry 에러 추적을 테스트하기 위한 API입니다. 호출 시 500 예외를 발생시킵니다.")
    @PostMapping("/test/sentry-error")
    fun testSentryError(): ApiResponse<Unit>
}
