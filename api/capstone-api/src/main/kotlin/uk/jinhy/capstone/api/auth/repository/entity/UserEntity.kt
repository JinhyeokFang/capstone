package uk.jinhy.capstone.api.auth.repository.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.jinhy.capstone.domain.user.model.User
import java.time.Instant

@Entity
@Table(name = "`user`")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint not null")
    val id: Long? = null,

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    val name: String,

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Size(max = 255)
    @Column(name = "password")
    val password: String? = null,

    @NotNull
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @NotNull
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),

    @Column(name = "last_login_at")
    val lastLoginAt: Instant? = null,

    @NotNull
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
) {
    fun toDomain(): User {
        return User(
            id = id,
            name = name,
            email = email,
            password = password,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastLoginAt = lastLoginAt,
            isActive = isActive,
        )
    }

    companion object {
        fun from(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                password = user.password,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
                lastLoginAt = user.lastLoginAt,
                isActive = user.isActive,
            )
        }
    }
}
