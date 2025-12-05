package uk.jinhy.capstone.api.auth.facade

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthLoginResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthMeResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthRefreshResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthSignUpResponseDto
import uk.jinhy.capstone.api.auth.service.AuthService
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginDto
import uk.jinhy.capstone.api.auth.service.dto.AuthSignUpDto
import uk.jinhy.capstone.domain.user.model.User

@Component
class AuthFacade(
    private val authService: AuthService,
) {
    @Transactional
    fun login(request: AuthLoginRequestDto): Pair<AuthLoginResponseDto, String> {
        val dto = AuthLoginDto(
            email = request.email,
            password = request.password,
        )
        val result = authService.login(dto)
        return Pair(AuthLoginResponseDto(accessToken = result.accessToken), result.refreshToken)
    }

    @Transactional
    fun signUp(request: AuthSignUpRequestDto): Pair<AuthSignUpResponseDto, String> {
        val dto = AuthSignUpDto(
            name = request.name,
            email = request.email,
            password = request.password,
        )
        val result = authService.signUp(dto)
        return Pair(AuthSignUpResponseDto(accessToken = result.accessToken), result.refreshToken)
    }

    fun getMe(user: User): AuthMeResponseDto {
        return AuthMeResponseDto(
            id = user.id,
            name = user.name,
            email = user.email,
        )
    }

    fun logout(refreshToken: String) {
        authService.logout(refreshToken)
    }

    fun refresh(refreshToken: String): AuthRefreshResponseDto {
        val accessToken = authService.refresh(refreshToken)
        return AuthRefreshResponseDto(accessToken = accessToken)
    }
}
