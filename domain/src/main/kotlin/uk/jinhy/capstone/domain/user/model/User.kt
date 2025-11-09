package uk.jinhy.capstone.domain.user.model

import java.time.Instant

data class User(
    val id: Long?,
    val name: String,
    val email: String,
    val password: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastLoginAt: Instant?,
    val isActive: Boolean,
) {
    fun login(): User {
        return this.copy(
            lastLoginAt = Instant.now(),
            updatedAt = Instant.now(),
        )
    }
}
