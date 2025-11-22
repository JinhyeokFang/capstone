package uk.jinhy.capstone.api.auth.facade

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.jinhy.capstone.api.auth.api.dto.request.AuthLoginRequestDto
import uk.jinhy.capstone.api.auth.api.dto.request.AuthSignUpRequestDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthLoginResponseDto
import uk.jinhy.capstone.api.auth.api.dto.response.AuthMeResponseDto
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
    fun login(request: AuthLoginRequestDto): AuthLoginResponseDto {
        val dto = AuthLoginDto(
            email = request.email,
            password = request.password,
        )
        val result = authService.login(dto)
        return AuthLoginResponseDto(accessToken = result.accessToken)
    }

    @Transactional
    fun signUp(request: AuthSignUpRequestDto): AuthSignUpResponseDto {
        val dto = AuthSignUpDto(
            name = request.name,
            email = request.email,
            password = request.password,
        )
        val result = authService.signUp(dto)
        return AuthSignUpResponseDto(accessToken = result.accessToken)
    }

    fun getMe(user: User): AuthMeResponseDto {
        return AuthMeResponseDto(
            id = user.id,
            name = user.name,
            email = user.email,
        )
    }
}
