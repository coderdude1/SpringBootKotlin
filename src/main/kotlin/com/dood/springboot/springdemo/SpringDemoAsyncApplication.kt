package com.dood.springboot.springdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
//@EnableAsync
class SpringDemoAsyncApplication

fun main(args: Array<String>) {
    runApplication<SpringDemoAsyncApplication>(*args)
}