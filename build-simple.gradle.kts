import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.jetbrains.kotlin.plugin.jpa")
        plugin("org.jetbrains.kotlin.kapt")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    group = "uk.jinhy.capstone"

    java {
        sourceCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        // BOM imports
        implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.8"))
        implementation(platform("com.fasterxml.jackson:jackson-bom:2.17.3"))
        implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2023.0.6"))

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
}
