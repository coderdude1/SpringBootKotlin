package com.dood.springboot.springdemo.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfig {
    //Some Coroutine Scopes to support structure concurrency
    @Bean
    fun ioScope(): CoroutineScope =
        CoroutineScope(Dispatchers.IO)

    @Bean
    fun defaultScope(): CoroutineScope =
        CoroutineScope(Dispatchers.Default)
}