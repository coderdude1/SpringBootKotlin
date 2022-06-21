package com.dood.springboot.springdemo.hello

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

//See testing notes
//@ContextConfiguration(classes = [(Components::class)])
/** This is using constructor injection, and kotest either requires passing in a lambda within
 *
 * class MyTests : FunSpec({
 *      tests
 * })
 *
 * or extend the testSpec then write the tests in an init block (this allows us to do constructor injection)
 */
@SpringBootTest //This causes the whole app to get fired up.  Not needed for the HelloRepository class.
class HelloRepositoryFunIntTest(helloRepository: HelloRepository) : FunSpec() {
    //Init blocks always execute after the constructor is executed
    init {
        test("get a single hello returns 1 line") {
            val result = helloRepository.getHellos(1)
            result.lines().size shouldBe 1
        }

        test("request zero hello returns 1 line") {
            val result = helloRepository.getHellos(0)
            result.lines().size shouldBe 1
        }

        test("request 10 hello returns 10 lines") {
            val result = helloRepository.getHellos(10)
            result.lines().size shouldBe 10
        }

        test("request negetive 1 hello returns 1 lines") {
            val result = helloRepository.getHellos(-1)
            result.lines().size shouldBe 1
        }
    }
}

/**
 * HelloRepository doesn't need any injection, as it has no dependencies
 */
class HelloRepositoryTest : StringSpec({
    val helloRepository = HelloRepository()

    "calling getHellos with a 0 returns 1 line" {
        val result = helloRepository.getHellos(0)
        result.lines().size shouldBe 1
    }
    "calling getHellos with a 1 returns 1 line" {
        val result = helloRepository.getHellos(1)
        result.lines().size shouldBe 1
    }
    "calling getHellows with a 10 returns 10 lines" {
        val result = helloRepository.getHellos(10)
        result.lines().size shouldBe 10
    }
})