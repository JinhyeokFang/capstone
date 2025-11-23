package uk.jinhy.capstone.api.auth.repository

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import uk.jinhy.capstone.api.support.IntegrationTestSupport
import uk.jinhy.capstone.domain.user.model.User
import java.time.Instant

class UserRepositoryTest : IntegrationTestSupport() {

    init {
        Given("사용자 저장 요청이 주어졌을 때") {
            val now = Instant.now()
            val user = User.create(
                name = "Test User",
                email = "test@example.com",
                password = "encodedPassword",
                createdAt = now,
                updatedAt = now
            )

            When("사용자를 저장하면") {
                Then("저장된 사용자를 반환한다") {
                    val savedUser = userRepository.save(user)

                    savedUser.id shouldBe user.id
                    savedUser.name shouldBe user.name
                    savedUser.email shouldBe user.email
                    savedUser.password shouldBe user.password
                    savedUser.isActive shouldBe true
                }
            }
        }

        Given("저장된 사용자가 있을 때") {
            val now = Instant.now()
            val user = User.create(
                name = "Find User",
                email = "find@example.com",
                password = "encodedPassword",
                createdAt = now,
                updatedAt = now
            )
            val savedUser = userRepository.save(user)

            When("이메일로 사용자를 조회하면") {
                Then("사용자를 찾는다") {
                    val foundUser = userRepository.findUserByEmail("find@example.com")

                    foundUser shouldNotBe null
                    foundUser!!.id shouldBe savedUser.id
                    foundUser.email shouldBe savedUser.email
                    foundUser.name shouldBe savedUser.name
                }
            }

            When("ID로 사용자를 조회하면") {
                Then("사용자를 찾는다") {
                    val foundUser = userRepository.findUserById(savedUser.id)

                    foundUser shouldNotBe null
                    foundUser!!.id shouldBe savedUser.id
                    foundUser.email shouldBe savedUser.email
                    foundUser.name shouldBe savedUser.name
                }
            }

            When("존재하지 않는 이메일로 조회하면") {
                Then("null을 반환한다") {
                    val foundUser = userRepository.findUserByEmail("notfound@example.com")

                    foundUser shouldBe null
                }
            }

            When("존재하지 않는 ID로 조회하면") {
                Then("null을 반환한다") {
                    val foundUser = userRepository.findUserById("non-existent-id")

                    foundUser shouldBe null
                }
            }
        }

        Given("사용자 업데이트 요청이 주어졌을 때") {
            val now = Instant.now()
            val user = User.create(
                name = "Update User",
                email = "update@example.com",
                password = "encodedPassword",
                createdAt = now,
                updatedAt = now
            )
            val savedUser = userRepository.save(user)

            When("사용자 정보를 업데이트하면") {
                val updatedUser = savedUser.copy(
                    name = "Updated Name",
                    updatedAt = Instant.now()
                )

                Then("업데이트된 정보가 저장된다") {
                    val result = userRepository.save(updatedUser)

                    result.id shouldBe savedUser.id
                    result.name shouldBe "Updated Name"
                    result.email shouldBe savedUser.email

                    // 데이터베이스에서 다시 조회하여 확인
                    val fetchedUser = userRepository.findUserById(savedUser.id)
                    fetchedUser shouldNotBe null
                    fetchedUser!!.name shouldBe "Updated Name"
                }
            }
        }

        Given("로그인 시나리오가 주어졌을 때") {
            val now = Instant.now()
            val user = User.create(
                name = "Login User",
                email = "login@example.com",
                password = "encodedPassword",
                createdAt = now,
                updatedAt = now
            )
            val savedUser = userRepository.save(user)

            When("사용자가 로그인하면") {
                val loggedInUser = savedUser.login()
                val updatedUser = userRepository.save(loggedInUser)

                Then("lastLoginAt이 업데이트된다") {
                    updatedUser.lastLoginAt shouldNotBe null
                    updatedUser.id shouldBe savedUser.id

                    // 데이터베이스에서 다시 조회하여 확인
                    val fetchedUser = userRepository.findUserById(savedUser.id)
                    fetchedUser shouldNotBe null
                    fetchedUser!!.lastLoginAt shouldNotBe null
                }
            }
        }
    }
}
