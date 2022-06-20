package com.dood.springboot.springdemo.hello

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

//See testing notes
//@ContextConfiguration(classes = [(Components::class)])
@SpringBootTest
class HelloRepositoryTest(helloRepository: HelloRepository) : FunSpec() {
    init {
        test("get a single hello returns 1") {
            val result = helloRepository.getHellos(1)
            result.lines().size shouldBe 1
        }
    }
}
