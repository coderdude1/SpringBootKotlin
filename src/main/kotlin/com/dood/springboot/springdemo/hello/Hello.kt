package com.dood.springboot.springdemo.hello

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random

/**
 * Simple blocking endpoint to start with.
 */
@RestController
class HelloController(val helloService: HelloService) {
    var logger: Logger = LoggerFactory.getLogger(HelloController::class.java)

    @GetMapping("/hello")
    fun hello(): String {
        logger.info("/hello just called")
        Thread.sleep(1000)
        val count = Random.nextInt(25)
        logger.info("awake and getting $count hellos")
        return helloService.getHellos(count)
    }
}

@Service
class HelloService(val helloRepository: HelloRepository) {
    fun getHellos(helloCount: Int): String {
        return helloRepository.getHellos(helloCount)
    }
}

@Repository
class HelloRepository {
    fun getHellos(helloCount: Int) : String {
        val count = if(helloCount == 0) 1 else helloCount
        var retval = ""
        for(i in 1..count) {
            retval += "hello<br>\n"
        }
        return retval
    }
}
