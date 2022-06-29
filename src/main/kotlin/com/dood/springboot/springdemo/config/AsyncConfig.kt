package com.dood.springboot.springdemo.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class AppConfig : AsyncConfigurer { //note there is an exception handler method that can be overridden
    override fun getAsyncExecutor(): Executor? {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 25
        executor.maxPoolSize = 150
        executor.setQueueCapacity(300)
        executor.setThreadNamePrefix("dood-threads-")
        executor.initialize()
        return executor
    }
}