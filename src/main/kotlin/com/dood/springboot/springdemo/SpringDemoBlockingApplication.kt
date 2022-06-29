package com.dood.springboot.springdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringDemoBlockingApplication

fun main(args: Array<String>) {
    runApplication<SpringDemoBlockingApplication>(*args)
}
