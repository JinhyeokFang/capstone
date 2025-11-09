package uk.jinhy.capstone.api.auth.facade

import org.springframework.stereotype.Component
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.api.dto.response.LoginResponse
import uk.jinhy.capstone.api.auth.service.AuthService

@Component
class AuthFacade(
    private val authService: AuthService,
) {
    fun login(request: LoginRequest): LoginResponse {
        return authService.login(request)
    }
}
