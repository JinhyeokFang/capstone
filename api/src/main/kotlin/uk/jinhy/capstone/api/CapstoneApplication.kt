package uk.jinhy.capstone.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["uk.jinhy.capstone"])
@EnableJpaRepositories(basePackages = ["uk.jinhy.capstone"])
@EntityScan(basePackages = ["uk.jinhy.capstone"])
@EnableFeignClients(basePackages = ["uk.jinhy.capstone"])
class CapstoneApplication

fun main(args: Array<String>) {
    runApplication<CapstoneApplication>(*args)
}
