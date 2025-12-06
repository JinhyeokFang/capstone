package uk.jinhy.capstone.util.jwt

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

object JwtUtil {
    private lateinit var secretKey: String
    private var accessTokenExpirationMillis: Long = 600000
    private var refreshTokenExpirationMillis: Long = 604800000

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    fun initialize(
        secretKey: String,
        accessTokenExpirationMillis: Long = 600000,
        refreshTokenExpirationMillis: Long = 604800000,
    ) {
        this.secretKey = secretKey
        this.accessTokenExpirationMillis = accessTokenExpirationMillis
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis
    }

    fun generateAccessToken(subject: String, claims: Map<String, Any> = emptyMap()): String {
        return generateToken(subject, claims, accessTokenExpirationMillis, TokenType.ACCESS)
    }

    fun generateRefreshToken(subject: String, claims: Map<String, Any> = emptyMap()): String {
        return generateToken(subject, claims, refreshTokenExpirationMillis, TokenType.REFRESH)
    }

    private fun generateToken(
        subject: String,
        claims: Map<String, Any>,
        expirationMillis: Long,
        tokenType: TokenType,
    ): String {
        val now = Date()
        val expiration = Date(now.time + expirationMillis)

        val builder = Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(expiration)
            .claim("type", tokenType.name)
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

    fun getTokenType(token: String): TokenType? {
        return try {
            val type = extractClaim(token, "type") as? String
            type?.let { TokenType.valueOf(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun getExpirationMillis(token: String): Long {
        return try {
            val claims = extractClaims(token)
            claims.expiration.time - claims.issuedAt.time
        } catch (e: Exception) {
            0L
        }
    }

    enum class TokenType {
        ACCESS, REFRESH
    }
}
