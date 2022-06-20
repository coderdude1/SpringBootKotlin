# Kotest Config
Need to add [the kotest sping extension](https://kotest.io/docs/extensions/spring.html) package to gradle.

note the artifact path is a bit different `io.kotest.extensions:kotest-extensions-spring`

The example in the kotest extension doc is a bit misleading.  Note the `@Components`.  That is actually a [class defined in the extension test example code](https://github.com/kotest/kotest-extensions-spring/blob/4d217f485ea832d2de5534a124ef6a2686bd7b7e/src/test/kotlin/io/kotest/extensions/spring/Components.kt), whcih is a configuration class (the docs to call that out but it is not clear.)

So some options (I think)
1. annotate the test class with @SpringBootTest.  This may turn things into an int test, ie start up a spring boot instance.  ANSWER: yes this runs the whole tomcat/netty container
2. create an analog to the @Components class above.  This might be better for simple unit tests where I inject stuff to test simple things.  https://github.com/kotest/kotest-examples-spring-webflux/blob/master/src/test/kotlin/io/kotest/example/spring/GreetingControllerTest.kt
3. @Components and mocks?
