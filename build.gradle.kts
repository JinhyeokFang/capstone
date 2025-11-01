import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val springBootVersion = "3.3.8"
    val springDependencyManagementVersion = "1.1.7"
    val kotlinVersion = "1.9.25"

    id("org.springframework.boot") version springBootVersion apply false
    id("io.spring.dependency-management") version springDependencyManagementVersion
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
    id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
}

allprojects {
    repositories {
        mavenCentral()
    }

    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.jetbrains.kotlin.plugin.jpa")
        plugin("org.jetbrains.kotlin.kapt")
        plugin("kotlin-noarg")
    }

    noArg {
        annotation("jakarta.persistence.Entity")
        annotation("jakarta.persistence.MappedSuperclass")
        annotation("jakarta.persistence.Embeddable")
    }

    repositories {
        mavenCentral()
    }

    ktlint {
        version.set("0.50.0")
        verbose.set(true)
        filter {
            exclude {
                it.file.path.contains("build/")
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    extra["queryDslVersion"] = "5.1.0"
}

subprojects {
    apply {
        plugin("kotlin-kapt")
        plugin("kotlin-jpa")
        plugin("kotlin-spring")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.jvm")
    }

    group = "uk.jinhy.capstone"

    java {
        sourceCompatibility = JavaVersion.VERSION_21
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.8")
            mavenBom("com.fasterxml.jackson:jackson-bom:2.17.3")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.6")
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
        testImplementation("io.kotest:kotest-assertions-core:5.8.0")
        testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        jvmArgs(
            "--add-opens=java.base/java.time=ALL-UNNAMED",
            "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
            "-Xmx2048m",
            "-Duser.timezone=UTC",
        )
        useJUnitPlatform()

        dependsOn(tasks.ktlintCheck)

        testLogging {
            events(
                FAILED,
                STANDARD_ERROR,
                SKIPPED,
            )
            exceptionFormat = FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}
