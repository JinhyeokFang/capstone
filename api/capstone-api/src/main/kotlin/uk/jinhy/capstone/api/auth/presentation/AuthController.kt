package uk.jinhy.capstone.api.auth.presentation

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.jinhy.capstone.api.auth.api.AuthApi
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthLoginResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthMeResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthRefreshResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthSignUpResponseDto
import uk.jinhy.capstone.api.auth.facade.AuthFacade
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.infra.security.annotation.CurrentUser
import uk.jinhy.capstone.util.exception.InternalServerException
import uk.jinhy.capstone.util.exception.UnauthorizedException
import uk.jinhy.capstone.util.response.ApiResponse

@RestController
class AuthController(
    private val authFacade: AuthFacade,
) : AuthApi {

    override fun login(
        @Valid @RequestBody request: AuthLoginRequestDto,
        response: HttpServletResponse,
    ): ApiResponse<AuthLoginResponseDto> {
        val (loginResponse, refreshToken) = authFacade.login(request)
        response.setHeader(
            "Set-Cookie",
            "refreshToken=$refreshToken; HttpOnly; Path=/; Max-Age=${7 * 24 * 60 * 60}; SameSite=Lax",
        )
        return ApiResponse.success(loginResponse)
    }

    override fun signUp(
        @Valid @RequestBody request: AuthSignUpRequestDto,
        response: HttpServletResponse,
    ): ApiResponse<AuthSignUpResponseDto> {
        val (signUpResponse, refreshToken) = authFacade.signUp(request)
        response.setHeader(
            "Set-Cookie",
            "refreshToken=$refreshToken; HttpOnly; Path=/; Max-Age=${7 * 24 * 60 * 60}; SameSite=Lax",
        )
        return ApiResponse.success(signUpResponse)
    }

    override fun me(@CurrentUser user: User): ApiResponse<AuthMeResponseDto> {
        val response = authFacade.getMe(user)
        return ApiResponse.success(response)
    }

    override fun logout(
        @CookieValue(name = "refreshToken", required = false) refreshToken: String?,
        response: HttpServletResponse,
    ): ApiResponse<Unit> {
        refreshToken?.let { authFacade.logout(it) }
        response.setHeader(
            "Set-Cookie",
            "refreshToken=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax",
        )
        return ApiResponse.success(Unit)
    }

    override fun refresh(
        @CookieValue(name = "refreshToken", required = false) refreshToken: String?,
    ): ApiResponse<AuthRefreshResponseDto> {
        if (refreshToken == null) {
            throw UnauthorizedException("리프레시 토큰이 없습니다.")
        }
        val refreshResponse = authFacade.refresh(refreshToken)
        return ApiResponse.success(refreshResponse)
    }

    override fun testSentryError(): ApiResponse<Unit> {
        throw InternalServerException("Sentry 에러 추적 테스트를 위한 예외입니다.")
    }
}
