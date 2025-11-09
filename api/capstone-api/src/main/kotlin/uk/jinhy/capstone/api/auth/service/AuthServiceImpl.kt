package uk.jinhy.capstone.api.auth.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.api.dto.response.LoginResponse
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.util.JwtUtil
import uk.jinhy.capstone.util.exception.BadRequestException

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
) : AuthService {

    @Transactional
    override fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findUserByEmail(request.email)
            ?: throw BadRequestException("사용자를 찾을 수 없습니다.", "USER_NOT_FOUND")

        if (user.password == null || !passwordEncoder.matches(request.password, user.password)) {
            throw BadRequestException("비밀번호가 일치하지 않습니다.", "PASSWORD_MISMATCH")
        }

        if (!user.isActive) {
            throw BadRequestException("비활성화된 사용자입니다.", "USER_INACTIVE")
        }

        val updatedUser = user.login()
        userRepository.saveUser(updatedUser)

        val accessToken = jwtUtil.generateToken(
            subject = updatedUser.id.toString(),
            claims = mapOf("email" to updatedUser.email),
        )

        return LoginResponse(accessToken = accessToken)
    }
}
