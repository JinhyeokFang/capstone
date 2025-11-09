package uk.jinhy.capstone.api

import org.redisson.spring.starter.RedissonAutoConfigurationV2
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(exclude = [RedisAutoConfiguration::class, RedissonAutoConfigurationV2::class])
@ComponentScan(basePackages = ["uk.jinhy.capstone.api", "uk.jinhy.capstone.infra", "uk.jinhy.capstone.util"])
@EnableJpaRepositories(basePackages = ["uk.jinhy.capstone.api"])
@EntityScan(basePackages = ["uk.jinhy.capstone.api"])
class CapstoneApplication

fun main(args: Array<String>) {
    runApplication<CapstoneApplication>(*args)
}
