import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("com.github.ryarnyah.querydsl") version "0.0.3"
    kotlin("kapt")
}

version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":common:util"))
    implementation(project(":common:infra"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.redisson:redisson-spring-boot-starter:3.24.3")

    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    implementation("com.querydsl:querydsl-sql:5.1.0")
    implementation("com.querydsl:querydsl-sql-spring:5.1.0")
    implementation("com.querydsl:querydsl-core:5.1.0")

    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    runtimeOnly("com.mysql:mysql-connector-j:9.2.0")

    testImplementation(project(":common:test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-assertions-json:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:mysql:1.19.3")
    testRuntimeOnly("com.h2database:h2")
}

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = true
jar.enabled = false

tasks.bootRun {
    jvmArgs(
        "-Xmx1024m",
        "-Duser.timezone=UTC",
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("querydsl.entityAccessors", "true")
    }
}
