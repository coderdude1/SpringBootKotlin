package com.dood.springboot.springdemo.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class SpringAsyncConfig : AsyncConfigurer { //note there is an exception handler method that can be overridden
    /**
     * Some notes and observations
     * 1.  Setting the queue size too high (close or greater than max pool size) results in the thread pool
     * not adding new threads, but rather pushing them into the queue.  Setting the queue size to 100
     * and the max pool size was somewhat faster than having it greater than the max pool size (my original
     * condition).
     *
     * I need to figure out the implications of having a small queue, ie what happens if I run out of threads?
     */
    override fun getAsyncExecutor(): Executor? {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 15
        executor.setQueueCapacity(5)
        executor.setThreadNamePrefix("dood-threads-")
        executor.initialize()
        return executor
    }
}