package uk.jinhy.capstone.api.auth.presentation

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.jinhy.capstone.api.auth.api.AuthApi
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthLoginResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthMeResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthSignUpResponseDto
import uk.jinhy.capstone.api.auth.facade.AuthFacade
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.infra.security.annotation.CurrentUser
import uk.jinhy.capstone.util.response.ApiResponse

@RestController
class AuthController(
    private val authFacade: AuthFacade,
) : AuthApi {

    override fun login(
        @Valid @RequestBody request: AuthLoginRequestDto,
    ): ApiResponse<AuthLoginResponseDto> {
        val response = authFacade.login(request)
        return ApiResponse.success(response)
    }

    override fun signUp(
        @Valid @RequestBody request: AuthSignUpRequestDto,
    ): ApiResponse<AuthSignUpResponseDto> {
        val response = authFacade.signUp(request)
        return ApiResponse.success(response)
    }

    override fun me(@CurrentUser user: User): ApiResponse<AuthMeResponseDto> {
        val response = authFacade.getMe(user)
        return ApiResponse.success(response)
    }
}
