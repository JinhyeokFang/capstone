package uk.jinhy.capstone.api.auth.repository

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.jinhy.capstone.api.auth.repository.entity.UserEntity
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.infra.querydsl.QuerydslConfig
import java.time.Instant

@DataJpaTest
@Import(QuerydslConfig::class, UserRepositoryImpl::class)
@ActiveProfiles("test")
class UserRepositoryTest(
    private val userRepository: UserRepository,
) : BehaviorSpec({
    extensions(SpringExtension)

    Given("사용자가 저장되어 있을 때") {
        val email = "test@example.com"
        val userEntity = UserEntity(
            name = "Test User",
            email = email,
            password = "encodedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true,
        )
        userRepository.save(userEntity)

        When("이메일로 사용자를 조회하면") {
            val foundUser = userRepository.findUserByEmail(email)

            Then("사용자가 반환된다") {
                foundUser.shouldNotBeNull()
                foundUser.email shouldBe email
                foundUser.name shouldBe "Test User"
            }
        }
    }

    Given("존재하지 않는 이메일로") {
        When("사용자를 조회하면") {
            val foundUser = userRepository.findUserByEmail("nonexistent@example.com")

            Then("null이 반환된다") {
                foundUser.shouldBeNull()
            }
        }
    }

    Given("새로운 사용자 정보가 주어졌을 때") {
        val user = User(
            id = null,
            name = "New User",
            email = "newuser@example.com",
            password = "encodedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = true,
        )

        When("사용자를 저장하면") {
            val savedUser = userRepository.saveUser(user)

            Then("ID가 할당된 사용자가 반환된다") {
                savedUser.id.shouldNotBeNull()
                savedUser.name shouldBe "New User"
                savedUser.email shouldBe "newuser@example.com"
            }

            Then("데이터베이스에서 조회할 수 있다") {
                val foundUser = userRepository.findUserByEmail("newuser@example.com")
                foundUser.shouldNotBeNull()
                foundUser.id shouldBe savedUser.id
            }
        }
    }

    Given("기존 사용자의 정보를 수정할 때") {
        val existingEntity = UserEntity(
            name = "Original User",
            email = "existing@example.com",
            password = "encodedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            lastLoginAt = null,
            isActive = true,
        )
        val saved = userRepository.save(existingEntity)

        val updatedUser = User(
            id = saved.id,
            name = "Updated User",
            email = "existing@example.com",
            password = "encodedPassword",
            createdAt = saved.createdAt,
            updatedAt = Instant.now(),
            lastLoginAt = Instant.now(),
            isActive = true,
        )

        When("사용자를 업데이트하면") {
            val result = userRepository.saveUser(updatedUser)

            Then("수정된 정보가 반영된다") {
                result.name shouldBe "Updated User"
                result.lastLoginAt.shouldNotBeNull()
            }

            Then("데이터베이스에서 수정된 정보를 조회할 수 있다") {
                val foundUser = userRepository.findUserByEmail("existing@example.com")
                foundUser.shouldNotBeNull()
                foundUser.name shouldBe "Updated User"
                foundUser.lastLoginAt.shouldNotBeNull()
            }
        }
    }
})
