package com.dood.springboot.springdemo.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
class AsyncController(@Autowired private val ioScope: CoroutineScope) {
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

    /**
     * Doing the `suspend` on a springboot controller implies it returns a Mono
     * https://docs.spring.io/spring-framework/docs/5.2.0.RELEASE/spring-framework-reference/languages.html#how-reactive-translates-to-coroutines
     * `fun handler(): Mono<Void> becomes suspend fun handler()`
     */
    @GetMapping("/suspendable")
    suspend fun getCoroutines() : String {
        logger.info("in GET coroutine")
        delay(500)
        logger.info("after delay")
        return "tada coroutines"
    }

    /**
     * Using GlobalScope usually isn't a good idea, as it isn't implementing structured concurrancy, which
     * can result in coroutine leaks, ie if multiple coroutines are in flight for a request, one throws
     * an exception the others will still execute, and can possibly be orpaned (if the endpoint returns due to
     * the exception.  Having said this the spring docs use GlobalScope in examples.
     * https://docs.spring.io/spring-framework/docs/5.2.0.RELEASE/spring-framework-reference/languages.html#controllers
     *
     * [This article](https://betterprogramming.pub/how-to-fire-and-forget-kotlin-coroutines-in-spring-boot-40f8204aac86)
     * has some good points on when to use what scope (at the conclusion section)
     */
    @GetMapping("/suspendableGlobalScoped")
    fun globalScopedAsync() = GlobalScope.async {//DelicateApi warning TODO fix this
        logger.info("in GlobalScope.async handler")
        delay(10)
        logger.info("after delay")
        "suspended in GlobalScope" //error when adding return due to `async`, this is the returned value
    }

    @GetMapping("/suspendableIoScoped")
    fun globalIoScopedAsync() = ioScope.async {
        logger.info("in ioScope.async handler")
        delay(10)
        logger.info("after delay")
        "suspended in ioScope" //error when adding return due to `async` this is the return value tho
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