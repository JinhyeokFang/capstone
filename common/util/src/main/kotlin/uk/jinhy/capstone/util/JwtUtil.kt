package uk.jinhy.capstone.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import uk.jinhy.capstone.util.exception.UnauthorizedException
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

class JwtUtil(
    private val secretKey: String,
    private val expirationMillis: Long = 86400000,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateToken(subject: String, claims: Map<String, Any> = emptyMap()): String {
        val now = Date()
        val expiration = Date(now.time + expirationMillis)

        val builder = Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)

        claims.forEach { (key, value) ->
            builder.claim(key, value)
        }

        return builder.compact()
    }

    fun extractSubject(token: String): String {
        return extractClaims(token).subject
    }

    fun extractClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw UnauthorizedException("JWT token has expired", e)
        } catch (e: MalformedJwtException) {
            throw UnauthorizedException("Invalid JWT token", e)
        } catch (e: UnsupportedJwtException) {
            throw UnauthorizedException("Unsupported JWT token", e)
        } catch (e: SecurityException) {
            throw UnauthorizedException("JWT signature validation failed", e)
        } catch (e: Exception) {
            throw UnauthorizedException("Failed to parse JWT token", e)
        }
    }

    fun extractClaim(token: String, claimName: String): Any? {
        return extractClaims(token)[claimName]
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = extractClaims(token)
            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }
}
