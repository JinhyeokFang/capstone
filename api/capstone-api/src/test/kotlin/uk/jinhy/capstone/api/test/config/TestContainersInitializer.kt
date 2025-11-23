package uk.jinhy.capstone.api.test.config

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

class TestContainersInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        private val mysqlContainer = MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .apply {
                withDatabaseName("capstone_test")
                withUsername("test")
                withPassword("test")
                withReuse(true)
                start()
            }

        private val redisContainer = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .apply {
                withExposedPorts(6379)
                withReuse(true)
                start()
            }
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "spring.datasource.url=${mysqlContainer.jdbcUrl}",
            "spring.datasource.username=${mysqlContainer.username}",
            "spring.datasource.password=${mysqlContainer.password}",
            "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
            "spring.data.redis.host=${redisContainer.host}",
            "spring.data.redis.port=${redisContainer.getMappedPort(6379)}",
            "jwt.secret-key=test-secret-key-for-testing-purposes-only-minimum-256-bits",
            "jwt.access-token-expiration-millis=3600000",
            "jwt.refresh-token-expiration-millis=604800000",
        ).applyTo(applicationContext.environment)
    }
}
