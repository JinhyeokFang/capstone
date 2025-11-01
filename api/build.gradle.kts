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
    implementation(project(":common-domain"))
    implementation(project(":common-util"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    implementation("com.querydsl:querydsl-sql:5.1.0")
    implementation("com.querydsl:querydsl-sql-codegen:5.1.0")
    implementation("com.querydsl:querydsl-core:5.1.0")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    implementation("com.querydsl:querydsl-collections")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.redisson:redisson-spring-boot-starter:3.24.3")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.github.openfeign:feign-okhttp")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    runtimeOnly("com.mysql:mysql-connector-j:9.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-assertions-json:5.9.1")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
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
