package uk.jinhy.capstone.infra.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import uk.jinhy.capstone.util.jwt.JwtUtil

@Component
class JwtAuthenticationFilter(
    private val currentUserService: CurrentUserService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractToken(request)

        if (token != null && JwtUtil.validateToken(token)) {
            try {
                val claims = JwtUtil.extractClaims(token)
                val userId = claims.subject

                val principal = currentUserService.loadUser(userId)
                if (principal != null) {
                    val authentication = UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_USER")),
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } catch (e: Exception) {
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
