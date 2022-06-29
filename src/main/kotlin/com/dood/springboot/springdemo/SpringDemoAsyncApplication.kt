package com.dood.springboot.springdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class SpringDemoAsyncApplication

fun main(args: Array<String>) {
    runApplication<SpringDemoAsyncApplication>(*args)
}