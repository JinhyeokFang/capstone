package uk.jinhy.capstone.api.auth.service

import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.api.dto.response.LoginResponse

interface AuthService {
    fun login(request: LoginRequest): LoginResponse
}
