package io.github.triplec

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = ["io.github.triplec"])
class TripleCApplication

fun main(args: Array<String>) {
    runApplication<TripleCApplication>(*args)
}
