package uk.jinhy.capstone.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["uk.jinhy.capstone"])
class CapstoneApplication

fun main(args: Array<String>) {
    runApplication<CapstoneApplication>(*args)
}
