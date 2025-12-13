package uk.jinhy.capstone.api.config

import jakarta.annotation.PostConstruct
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestContainersConfig(
    private val environment: ConfigurableEnvironment,
) {
    private val redisContainer = GenericContainer(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379)
        .withReuse(true)

    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer<*> {
        return MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)
    }

    @PostConstruct
    fun init() {
        if (!redisContainer.isRunning) {
            redisContainer.start()
            val properties = mapOf(
                "spring.data.redis.host" to redisContainer.host,
                "spring.data.redis.port" to redisContainer.getMappedPort(6379).toString(),
            )
            environment.propertySources.addFirst(
                MapPropertySource("testcontainers-redis", properties),
            )
        }
    }
}
