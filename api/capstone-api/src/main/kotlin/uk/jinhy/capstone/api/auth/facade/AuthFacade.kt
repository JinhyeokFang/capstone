package uk.jinhy.capstone.api.auth.facade

import org.springframework.stereotype.Component
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.api.dto.request.SignUpRequest
import uk.jinhy.capstone.api.auth.api.dto.response.LoginResponse
import uk.jinhy.capstone.api.auth.api.dto.response.MeResponse
import uk.jinhy.capstone.api.auth.api.dto.response.SignUpResponse
import uk.jinhy.capstone.api.auth.service.AuthService

@Component
class AuthFacade(
    private val authService: AuthService,
) {
    fun login(request: LoginRequest): LoginResponse {
        return authService.login(request)
    }

    fun signUp(request: SignUpRequest): SignUpResponse {
        return authService.signUp(request)
    }

    fun getMe(userId: Long): MeResponse {
        return authService.getMe(userId)
    }
}
