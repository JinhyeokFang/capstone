import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
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

    implementation("org.springframework.boot:spring-boot-starter-web:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.3.8")
    implementation("org.springframework.boot:spring-boot-starter-security:3.3.8")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.3")

    // Temporarily commented out due to parent POM dependency issues
    // implementation("org.redisson:redisson-spring-boot-starter:3.24.3")

    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    // Temporarily commented out due to BOM dependency issues
    // kapt("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Temporarily commented out due to BOM dependency issues
    // implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Temporarily commented out due to parent POM dependency issues
    // runtimeOnly("io.micrometer:micrometer-registry-prometheus:1.13.9")

    runtimeOnly("com.mysql:mysql-connector-j:9.2.0")

    testImplementation(project(":common:test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-assertions-json:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
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
