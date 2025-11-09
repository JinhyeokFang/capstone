package uk.jinhy.capstone.api.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.jinhy.capstone.api.auth.repository.entity.UserEntity
import uk.jinhy.capstone.domain.user.model.User

interface UserRepository : JpaRepository<UserEntity, Long>, UserRepositoryCustom

interface UserRepositoryCustom {
    fun findUserByEmail(email: String): User?
    fun saveUser(user: User): User
}
