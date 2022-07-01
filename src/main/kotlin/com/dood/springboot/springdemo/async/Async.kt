package com.dood.springboot.springdemo.async

import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

/**
 * non-blocking/async but not reactive endpoints to better understand the differences and options of
 * springboot and async.
 */

@RestController()
class AsyncController {
    val logger: Logger = LoggerFactory.getLogger(AsyncController::class.java)

    /**
     * This endpoint will get spawned on another thread due to the @Async annotation.  If I don't return a
     * CompletableFuture (ie just return String), it still runs on a differnt thread but the client does not
     * get a response.
     */
    @GetMapping("/async")
    @Async
    fun getAsync() : CompletableFuture<String> {
        logger.info("in GET async")
        sleep(500)
        logger.info("after sleep")
        return CompletableFuture.completedFuture("This is the async return completable future")
    }

    @GetMapping("/suspendable")
    suspend fun getCoroutines() : String {
        logger.info("in GET coroutine")
        delay(500)
        logger.info("after delay")
        return "tada corutines"
    }

    @GetMapping("/not_async")
    fun getNotAsync() : String {
        logger.info("in GET NotAsync")
        sleep(500)
        logger.info("after sleep")
        return "blah"
    }
}

@Component
class FilterLogger : Filter {
    private val logger: Logger = LoggerFactory.getLogger(FilterLogger::class.java)

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        logger.info("start of servlet filter")
        sleep(200)
        logger.info("filter awake now")
        chain?.doFilter(request, response) //continue the chain
    }
}

fun sleep(sleepTimeMs: Long) {
    try {
        Thread.sleep(sleepTimeMs) //can't use 'delay' as that requires caller to be suspendable
    } catch (e : InterruptedException){
        println("sleep awake")
    }
}