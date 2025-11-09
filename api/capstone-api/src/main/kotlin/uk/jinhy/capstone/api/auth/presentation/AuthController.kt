package uk.jinhy.capstone.api.auth.presentation

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.jinhy.capstone.api.auth.api.AuthApi
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.api.dto.response.LoginResponse
import uk.jinhy.capstone.api.auth.facade.AuthFacade
import uk.jinhy.capstone.util.response.ApiResponse

@RestController
class AuthController(
    private val authFacade: AuthFacade,
) : AuthApi {

    override fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ApiResponse<LoginResponse> {
        val response = authFacade.login(request)
        return ApiResponse.success(response)
    }
}
