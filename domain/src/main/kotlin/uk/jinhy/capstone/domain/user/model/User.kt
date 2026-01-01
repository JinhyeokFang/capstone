package uk.jinhy.capstone.domain.user.model

import uk.jinhy.capstone.util.string.StringUtil
import java.time.Instant

data class User(
    val id: String,
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

    companion object {
        fun create(
            name: String,
            email: String,
            password: String?,
            createdAt: Instant,
            updatedAt: Instant,
        ): User {
            return User(
                id = StringUtil.generateUuid(),
                name = name,
                email = email,
                password = password,
                createdAt = createdAt,
                updatedAt = updatedAt,
                lastLoginAt = null,
                isActive = true,
            )
        }
    }
}
