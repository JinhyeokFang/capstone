package uk.jinhy.capstone.infra.auth

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import uk.jinhy.capstone.util.jwt.JwtUtil

@Configuration
class AuthConfig(
    @Value("\${jwt.secret-key:QWERTYUIOPASDFGHJKLZXCVBNM1234567890QWERTYUIOPASDFGHJKLZXCVBNM1234567890}")
    private val secretKey: String,
    @Value("\${jwt.access-token-expiration-millis:600000}")
    private val accessTokenExpirationMillis: Long,
    @Value("\${jwt.refresh-token-expiration-millis:604800000}")
    private val refreshTokenExpirationMillis: Long,
) {

    @PostConstruct
    fun initJwtUtil() {
        JwtUtil.initialize(secretKey, accessTokenExpirationMillis, refreshTokenExpirationMillis)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
