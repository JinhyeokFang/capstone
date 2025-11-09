package uk.jinhy.capstone.api

import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class CapstoneApplicationTests : StringSpec({
    "context loads" {
    }
})
