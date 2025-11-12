package uk.jinhy.capstone.api.auth.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.jinhy.capstone.api.auth.api.dto.request.LoginRequest
import uk.jinhy.capstone.api.auth.api.dto.request.SignUpRequest
import uk.jinhy.capstone.api.auth.api.dto.response.LoginResponse
import uk.jinhy.capstone.api.auth.api.dto.response.MeResponse
import uk.jinhy.capstone.api.auth.api.dto.response.SignUpResponse
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.util.JwtUtil
import uk.jinhy.capstone.util.exception.BadRequestException
import uk.jinhy.capstone.util.exception.ConflictException
import uk.jinhy.capstone.util.exception.NotFoundException
import java.time.Instant

interface AuthService {
    fun login(request: LoginRequest): LoginResponse
    fun signUp(request: SignUpRequest): SignUpResponse
    fun getMe(userId: Long): MeResponse
}

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

    @Transactional
    override fun signUp(request: SignUpRequest): SignUpResponse {
        val existingUser = userRepository.findUserByEmail(request.email)
        if (existingUser != null) {
            throw ConflictException("이미 존재하는 이메일입니다.", "EMAIL_ALREADY_EXISTS")
        }

        val encodedPassword = passwordEncoder.encode(request.password)
        val now = Instant.now()

        val newUser = User(
            id = null,
            name = request.name,
            email = request.email,
            password = encodedPassword,
            createdAt = now,
            updatedAt = now,
            lastLoginAt = null,
            isActive = true,
        )

        val savedUser = userRepository.saveUser(newUser)

        val accessToken = jwtUtil.generateToken(
            subject = savedUser.id.toString(),
            claims = mapOf("email" to savedUser.email),
        )

        return SignUpResponse(accessToken = accessToken)
    }

    override fun getMe(userId: Long): MeResponse {
        val user = userRepository.findUserById(userId)
            ?: throw NotFoundException("사용자를 찾을 수 없습니다.", "USER_NOT_FOUND")

        return MeResponse(
            id = user.id!!,
            name = user.name,
            email = user.email,
        )
    }
}
