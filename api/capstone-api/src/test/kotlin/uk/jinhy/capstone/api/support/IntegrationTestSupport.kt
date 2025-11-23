package uk.jinhy.capstone.api.support

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import uk.jinhy.capstone.api.auth.repository.UserRepository
import uk.jinhy.capstone.infra.auth.RefreshTokenBlocklistService

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainersConfig::class)
@Transactional
abstract class IntegrationTestSupport : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var refreshTokenBlocklistService: RefreshTokenBlocklistService

    @Autowired
    protected lateinit var applicationContext: ApplicationContext
}
