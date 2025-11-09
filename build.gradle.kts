import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:3.3.8")
        classpath("io.spring.gradle:dependency-management-plugin:1.1.7")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.25")
        classpath("org.jetbrains.kotlin:kotlin-allopen:1.9.25")
        classpath("org.jetbrains.kotlin:kotlin-noarg:1.9.25")
    }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    group = "uk.jinhy.capstone"
}

subprojects {
    group = "uk.jinhy.capstone"

    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "kotlin-noarg")

    configure<org.jetbrains.kotlin.noarg.gradle.NoArgExtension> {
        annotation("jakarta.persistence.Entity")
        annotation("jakarta.persistence.MappedSuperclass")
        annotation("jakarta.persistence.Embeddable")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
    }

    configurations {
        getByName("compileOnly") {
            extendsFrom(configurations.getByName("annotationProcessor"))
        }
    }

    dependencies {
        add("implementation", "org.jetbrains.kotlin:kotlin-reflect:1.9.25")
        add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.25")

        add("testImplementation", "io.kotest:kotest-runner-junit5:5.8.0")
        add("testImplementation", "io.kotest:kotest-assertions-core:5.8.0")
        add("testImplementation", "io.kotest.extensions:kotest-extensions-spring:1.1.3")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    extra["queryDslVersion"] = "5.1.0"

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
