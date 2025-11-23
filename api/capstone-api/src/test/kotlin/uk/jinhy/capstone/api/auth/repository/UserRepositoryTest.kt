package uk.jinhy.capstone.api.auth.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.jinhy.capstone.domain.user.model.User
import uk.jinhy.capstone.infra.querydsl.QuerydslConfig
import java.time.Instant

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig::class, UserRepositoryImpl::class)
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest(
    private val userRepository: UserRepository,
) : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    given("사용자 저장") {
        `when`("새로운 사용자를 저장하면") {
            val now = Instant.now()
            val user = User.create(
                name = "테스트 유저",
                email = "test@example.com",
                password = "encoded-password",
                createdAt = now,
                updatedAt = now,
            )

            then("사용자가 저장되고 반환된다") {
                val savedUser = userRepository.save(user)

                savedUser.id shouldBe user.id
                savedUser.name shouldBe "테스트 유저"
                savedUser.email shouldBe "test@example.com"
                savedUser.password shouldBe "encoded-password"
                savedUser.isActive shouldBe true
                savedUser.lastLoginAt shouldBe null
            }
        }

        `when`("기존 사용자를 업데이트하면") {
            val now = Instant.now()
            val user = User.create(
                name = "테스트 유저",
                email = "update@example.com",
                password = "encoded-password",
                createdAt = now,
                updatedAt = now,
            )

            val savedUser = userRepository.save(user)
            val updatedUser = savedUser.copy(name = "업데이트된 유저")

            then("사용자가 업데이트되고 반환된다") {
                val result = userRepository.save(updatedUser)

                result.id shouldBe savedUser.id
                result.name shouldBe "업데이트된 유저"
                result.email shouldBe "update@example.com"
            }
        }
    }

    given("이메일로 사용자 조회") {
        `when`("존재하는 이메일로 조회하면") {
            val now = Instant.now()
            val user = User.create(
                name = "이메일 테스트 유저",
                email = "email@example.com",
                password = "encoded-password",
                createdAt = now,
                updatedAt = now,
            )

            userRepository.save(user)

            then("사용자를 반환한다") {
                val foundUser = userRepository.findUserByEmail("email@example.com")

                foundUser shouldNotBe null
                foundUser!!.email shouldBe "email@example.com"
                foundUser.name shouldBe "이메일 테스트 유저"
            }
        }

        `when`("존재하지 않는 이메일로 조회하면") {
            then("null을 반환한다") {
                val foundUser = userRepository.findUserByEmail("nonexistent@example.com")

                foundUser shouldBe null
            }
        }
    }

    given("ID로 사용자 조회") {
        `when`("존재하는 ID로 조회하면") {
            val now = Instant.now()
            val user = User.create(
                name = "ID 테스트 유저",
                email = "id@example.com",
                password = "encoded-password",
                createdAt = now,
                updatedAt = now,
            )

            val savedUser = userRepository.save(user)

            then("사용자를 반환한다") {
                val foundUser = userRepository.findUserById(savedUser.id)

                foundUser shouldNotBe null
                foundUser!!.id shouldBe savedUser.id
                foundUser.name shouldBe "ID 테스트 유저"
            }
        }

        `when`("존재하지 않는 ID로 조회하면") {
            then("null을 반환한다") {
                val foundUser = userRepository.findUserById("nonexistent-id")

                foundUser shouldBe null
            }
        }
    }

    given("Table ID로 사용자 조회") {
        `when`("존재하는 Table ID로 조회하면") {
            val now = Instant.now()
            val user = User.create(
                name = "Table ID 테스트 유저",
                email = "tableid@example.com",
                password = "encoded-password",
                createdAt = now,
                updatedAt = now,
            )

            userRepository.save(user)

            val foundByEmail = userRepository.findUserByEmail("tableid@example.com")
            val tableId = 1L // First saved user should have table ID 1

            then("사용자를 반환한다") {
                val foundUser = userRepository.findUserByTableId(tableId)

                foundUser shouldNotBe null
            }
        }

        `when`("존재하지 않는 Table ID로 조회하면") {
            then("null을 반환한다") {
                val foundUser = userRepository.findUserByTableId(999999L)

                foundUser shouldBe null
            }
        }
    }
})
