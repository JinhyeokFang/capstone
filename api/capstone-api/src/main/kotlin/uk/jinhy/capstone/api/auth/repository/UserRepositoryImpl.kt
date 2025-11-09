package uk.jinhy.capstone.api.auth.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import uk.jinhy.capstone.api.auth.repository.entity.QUserEntity.userEntity
import uk.jinhy.capstone.api.auth.repository.entity.UserEntity
import uk.jinhy.capstone.domain.user.model.User

@Repository
class UserRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    @PersistenceContext private val entityManager: EntityManager,
) : UserRepositoryCustom {

    override fun findUserByEmail(email: String): User? {
        val entity = queryFactory
            .selectFrom(userEntity)
            .where(userEntity.email.eq(email))
            .fetchOne()
        return entity?.toDomain()
    }

    @Transactional
    override fun saveUser(user: User): User {
        val entity = UserEntity.from(user)
        val savedEntity = entityManager.merge(entity)
        entityManager.flush()
        entityManager.clear()
        val refreshedEntity = entityManager.find(UserEntity::class.java, savedEntity.id)
        return refreshedEntity!!.toDomain()
    }
}
