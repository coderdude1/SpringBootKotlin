import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.asciidoctor.convert") version "1.5.8"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.dood.springboot"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["snippetsDir"] = file("build/generated-snippets")
extra["testcontainersVersion"] = "1.17.2"

val kotestVersion = project.properties["kotestVersion"]
val kotestSpringExtensionVersion = project.properties["kotestSpringExtensionVersion"]

dependencies {
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    //disabled to remove these things from the classpath so I don't get errors/default secured endpoints
    //will add them back when I'm ready to work with these featyures
    //implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
   // implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    //implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    //implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    runtimeOnly("io.micrometer:micrometer-registry-datadog")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:$kotestSpringExtensionVersion")

}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

//An option is to add all of these to a `tasks` block rather than prepending
//each with a `tasks.`
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    //outputs.dir(snippetsDir) //original created by initializer fails
    project.property("snippetsDir")!!.let { outputs.dir(it) }
}

tasks.asciidoctor {
    //inputs.dir(snippetsDir) //originally created by initializer, fails
    project.property("snippetsDir")!!.let { inputs.dir(it) }
    dependsOn(tasks.test) //had to change from '(test)
}
