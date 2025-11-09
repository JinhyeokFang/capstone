package uk.jinhy.capstone.infra.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import uk.jinhy.capstone.util.JwtUtil

@Configuration
class AuthConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtUtil(
        @Value("\${jwt.secret-key:your-secret-key-change-this-in-production-minimum-32-characters}") secretKey: String,
        @Value("\${jwt.expiration-millis:86400000}") expirationMillis: Long,
    ): JwtUtil {
        return JwtUtil(secretKey, expirationMillis)
    }
}
