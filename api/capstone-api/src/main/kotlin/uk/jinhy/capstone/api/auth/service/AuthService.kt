package uk.jinhy.capstone.api.auth.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginDto
import uk.jinhy.capstone.api.auth.service.dto.AuthLoginResultDto
import uk.jinhy.capstone.api.auth.service.dto.AuthSignUpDto
import uk.jinhy.capstone.api.auth.service.dto.AuthSignUpResultDto
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.util.exception.BadRequestException
import uk.jinhy.capstone.util.exception.ConflictException
import uk.jinhy.capstone.util.jwt.JwtUtil
import java.time.Instant

interface AuthService {
    fun login(dto: AuthLoginDto): AuthLoginResultDto
    fun signUp(dto: AuthSignUpDto): AuthSignUpResultDto
}

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
) : AuthService {

    override fun login(dto: AuthLoginDto): AuthLoginResultDto {
        val user = userRepository.findUserByEmail(dto.email)
            ?: throw BadRequestException("사용자를 찾을 수 없습니다.", "USER_NOT_FOUND")

        if (user.password == null || !passwordEncoder.matches(dto.password, user.password)) {
            throw BadRequestException("비밀번호가 일치하지 않습니다.", "PASSWORD_MISMATCH")
        }

        if (!user.isActive) {
            throw BadRequestException("비활성화된 사용자입니다.", "USER_INACTIVE")
        }

        val updatedUser = user.login()
        userRepository.save(updatedUser)

        val accessToken = jwtUtil.generateToken(
            subject = updatedUser.id,
            claims = mapOf("email" to updatedUser.email),
        )

        return AuthLoginResultDto(accessToken = accessToken)
    }

    override fun signUp(dto: AuthSignUpDto): AuthSignUpResultDto {
        val existingUser = userRepository.findUserByEmail(dto.email)
        if (existingUser != null) {
            throw ConflictException("이미 존재하는 이메일입니다.", "EMAIL_ALREADY_EXISTS")
        }

        val encodedPassword = passwordEncoder.encode(dto.password)
        val now = Instant.now()

        val newUser = User.create(
            name = dto.name,
            email = dto.email,
            password = encodedPassword,
            createdAt = now,
            updatedAt = now,
        )

        val savedUser = userRepository.save(newUser)

        val accessToken = jwtUtil.generateToken(
            subject = savedUser.id,
            claims = mapOf("email" to savedUser.email),
        )

        return AuthSignUpResultDto(accessToken = accessToken)
    }
}
