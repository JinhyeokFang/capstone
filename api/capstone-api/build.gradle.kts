import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("com.github.ryarnyah.querydsl") version "0.0.3"
    id("io.sentry.jvm.gradle") version "5.12.2"
    kotlin("kapt")
}

version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

extra["testcontainers.version"] = "1.20.5"

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
    implementation("p6spy:p6spy:3.9.1")
    implementation("io.sentry:sentry-spring-boot-starter-jakarta:7.15.0")
    implementation("io.sentry:sentry-jdbc:7.15.0")

    testImplementation(project(":common:test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-assertions-json:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.3.0")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-bom:2.0.2")
    testImplementation("org.testcontainers:testcontainers:2.0.2")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.2")
    testImplementation("org.testcontainers:testcontainers-mysql:2.0.2")
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
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("querydsl.entityAccessors", "true")
    }
    useBuildCache = false
}

tasks.matching { it.name.startsWith("kapt") && it.name.contains("Test") }.configureEach {
    enabled = false
}

sentry {
    val sentryAuthToken = System.getenv("SENTRY_AUTH_TOKEN")
    includeSourceContext.set(!sentryAuthToken.isNullOrBlank())
    org.set("jinhyeokfang")
    projectName.set("spring-boot")
    authToken.set(sentryAuthToken)

    tasks.named("sentryBundleSourcesJava") {
        onlyIf { !sentryAuthToken.isNullOrBlank() }
    }
}
