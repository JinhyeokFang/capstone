package uk.jinhy.capstone.api.auth.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.jinhy.capstone.api.auth.repository.entity.QUserEntity.userEntity
import uk.jinhy.capstone.api.auth.repository.entity.UserEntity
import uk.jinhy.capstone.api.auth.repository.entity.toDomain
import uk.jinhy.capstone.domain.user.model.User

interface UserRepository {
    fun findUserByEmail(email: String): User?
    fun findUserByTableId(tableId: Long): User?
    fun findUserById(id: String): User?
    fun save(user: User): User
}

interface JpaUserEntityRepository : JpaRepository<UserEntity, Long>

@Repository
class UserRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val jpaUserEntityRepository: JpaUserEntityRepository,
) : UserRepository {

    override fun findUserByEmail(email: String): User? {
        val entity = queryFactory
            .selectFrom(userEntity)
            .where(userEntity.email.eq(email))
            .fetchOne()
        return entity?.toDomain()
    }

    override fun findUserByTableId(tableId: Long): User? {
        val entity = queryFactory
            .selectFrom(userEntity)
            .where(userEntity.tableId.eq(tableId))
            .fetchOne()
        return entity?.toDomain()
    }

    override fun findUserById(id: String): User? {
        val entity = queryFactory
            .selectFrom(userEntity)
            .where(userEntity.id.eq(id))
            .fetchOne()
        return entity?.toDomain()
    }

    override fun save(user: User): User {
        val tableId = queryFactory
            .select(userEntity.tableId)
            .from(userEntity)
            .where(userEntity.id.eq(user.id))
            .fetchOne()

        val entity = UserEntity(
            tableId = tableId,
            id = user.id,
            name = user.name,
            email = user.email,
            password = user.password,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            lastLoginAt = user.lastLoginAt,
            isActive = user.isActive,
        )

        val savedEntity = jpaUserEntityRepository.save(entity)
        return savedEntity.toDomain()
    }
}
