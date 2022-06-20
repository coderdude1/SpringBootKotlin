# General Notes
I'll probably start fleshing out stuff here, then splitting it up into sub documents.

# Gradle Stuff
Gradle has a lot of ways of doing the same thing.  Some things that look interesting include

* Toolchains [gradle docs](https://docs.gradle.org/current/userguide/toolchains.html) and [kotlin docs](https://kotlinlang.org/docs/gradle.html#gradle-java-toolchains-support)
* block declaration of things like `tasks` vs using a `tasks.<taskname>` for each one
* Adding kotest, and configuring the kotlin compiler vs kotlin test compiler versions.  Currently using a jetbrains task `KotlinCompile` vs something like

```groovy
tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
```

# Spring Boot Modules
## Datadog
I get an error on startup:

```
Invalid Micrometer configuration detected:
  - management.metrics.export.datadog.apiKey was 'null' but it is required

```

# Cool Stuff
## Async IO

## ReactiveIO

## Database Access (blocking, async, and reactive)

## Netty vs Tomcat
By default adding the springboot-webflux to the classpath should force springboot to netty.  My first stuff looks like it is tomcat even tho I added that on the dependency graph.  I assume since I don't actually do any webflux ATM thats why tomcat is still getting used

    2022-06-20 14:03:34.218  INFO 97232 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''