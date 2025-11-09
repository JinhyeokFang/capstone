#!/bin/bash
set -e

MAVEN_REPO="$HOME/.m2/repository"
mkdir -p "$MAVEN_REPO"

# Download a dependency and its POM
download_artifact() {
    local group_id=$1
    local artifact_id=$2
    local version=$3
    local classifier=$4

    local group_path=$(echo "$group_id" | tr '.' '/')
    local base_path="$MAVEN_REPO/$group_path/$artifact_id/$version"
    local base_url="https://repo.maven.apache.org/maven2/$group_path/$artifact_id/$version"

    mkdir -p "$base_path"

    # Download POM
    local pom_file="$base_path/${artifact_id}-${version}.pom"
    if [ ! -f "$pom_file" ]; then
        echo "Downloading POM: $group_id:$artifact_id:$version"
        curl -sf "$base_url/${artifact_id}-${version}.pom" -o "$pom_file" || echo "Failed to download POM"
    fi

    # Download JAR
    local jar_file="$base_path/${artifact_id}-${version}${classifier:+-}${classifier}.jar"
    if [ ! -f "$jar_file" ]; then
        echo "Downloading JAR: $group_id:$artifact_id:$version${classifier:+:}${classifier}"
        curl -sf "$base_url/${artifact_id}-${version}${classifier:+-}${classifier}.jar" -o "$jar_file" || echo "Failed to download JAR"
    fi
}

# Download Spring Boot plugin and its dependencies
download_artifact org.springframework.boot spring-boot-gradle-plugin 3.3.8 ""
download_artifact org.springframework.boot spring-boot-buildpack-platform 3.3.8 ""
download_artifact org.springframework.boot spring-boot-loader-tools 3.3.8 ""
download_artifact org.springframework.boot spring-boot 3.3.8 ""
download_artifact org.springframework spring-core 6.1.14 ""
download_artifact org.springframework spring-context 6.1.14 ""

# Kotlin dependencies
download_artifact org.jetbrains.kotlin kotlin-gradle-plugin 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-gradle-plugin-api 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-gradle-plugin-model 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-compiler-embeddable 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-stdlib 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-stdlib-jdk8 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-stdlib-jdk7 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-reflect 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-allopen 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-noarg 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-script-runtime 1.9.25 ""

# Spring Boot dependencies BOM
download_artifact org.springframework.boot spring-boot-dependencies 3.3.8 ""
download_artifact org.springframework.boot spring-boot-starter 3.3.8 ""
download_artifact org.springframework.boot spring-boot-starter-web 3.3.8 ""
download_artifact org.springframework.boot spring-boot-starter-data-jpa 3.3.8 ""
download_artifact org.springframework.boot spring-boot-starter-security 3.3.8 ""
download_artifact org.springframework.boot spring-boot-starter-validation 3.3.8 ""
download_artifact org.springframework.boot spring-boot-starter-actuator 3.3.8 ""
download_artifact org.springframework.boot spring-boot-starter-test 3.3.8 ""
download_artifact org.springframework.boot spring-boot-autoconfigure 3.3.8 ""

# Spring Framework
download_artifact org.springframework spring-beans 6.1.14 ""
download_artifact org.springframework spring-web 6.1.14 ""
download_artifact org.springframework spring-webmvc 6.1.14 ""
download_artifact org.springframework spring-aop 6.1.14 ""
download_artifact org.springframework spring-expression 6.1.14 ""
download_artifact org.springframework spring-jcl 6.1.14 ""
download_artifact org.springframework spring-tx 6.1.14 ""
download_artifact org.springframework spring-orm 6.1.14 ""
download_artifact org.springframework spring-jdbc 6.1.14 ""
download_artifact org.springframework spring-test 6.1.14 ""

# Spring Security
download_artifact org.springframework.security spring-security-core 6.3.4 ""
download_artifact org.springframework.security spring-security-config 6.3.4 ""
download_artifact org.springframework.security spring-security-web 6.3.4 ""

# Spring Data
download_artifact org.springframework.data spring-data-jpa 3.3.5 ""
download_artifact org.springframework.data spring-data-commons 3.3.5 ""

# Jakarta EE
download_artifact jakarta.persistence jakarta.persistence-api 3.1.0 ""
download_artifact jakarta.annotation jakarta.annotation-api 2.1.1 ""
download_artifact jakarta.validation jakarta.validation-api 3.0.2 ""
download_artifact jakarta.servlet jakarta.servlet-api 6.0.0 ""

# Hibernate
download_artifact org.hibernate.orm hibernate-core 6.5.3.Final ""
download_artifact org.hibernate.common hibernate-commons-annotations 6.0.6.Final ""

# Jackson
download_artifact com.fasterxml.jackson.core jackson-databind 2.17.3 ""
download_artifact com.fasterxml.jackson.core jackson-core 2.17.3 ""
download_artifact com.fasterxml.jackson.core jackson-annotations 2.17.3 ""
download_artifact com.fasterxml.jackson.module jackson-module-kotlin 2.17.3 ""
download_artifact com.fasterxml.jackson.datatype jackson-datatype-jsr310 2.17.3 ""

# Logging
download_artifact ch.qos.logback logback-classic 1.5.12 ""
download_artifact ch.qos.logback logback-core 1.5.12 ""
download_artifact org.slf4j slf4j-api 2.0.16 ""

# Testing
download_artifact io.kotest kotest-runner-junit5 5.9.1 ""
download_artifact io.kotest kotest-assertions-core 5.9.1 ""
download_artifact io.kotest kotest-framework-engine 5.9.1 ""
download_artifact io.kotest.extensions kotest-extensions-spring 1.3.0 ""
download_artifact org.junit.jupiter junit-jupiter 5.10.5 ""
download_artifact org.junit.jupiter junit-jupiter-api 5.10.5 ""
download_artifact org.junit.jupiter junit-jupiter-engine 5.10.5 ""

# QueryDSL
download_artifact com.querydsl querydsl-jpa 5.1.0 jakarta
download_artifact com.querydsl querydsl-apt 5.1.0 jakarta
download_artifact com.querydsl querydsl-core 5.1.0 ""

# Commons
download_artifact org.apache.commons commons-compress 1.27.1 ""
download_artifact commons-logging commons-logging 1.3.4 ""

# MySQL
download_artifact com.mysql mysql-connector-j 9.2.0 ""

# Tomcat embed
download_artifact org.apache.tomcat.embed tomcat-embed-core 10.1.33 ""
download_artifact org.apache.tomcat.embed tomcat-embed-websocket 10.1.33 ""
download_artifact org.apache.tomcat.embed tomcat-embed-el 10.1.33 ""

# Micrometer
download_artifact io.micrometer micrometer-core 1.13.7 ""
download_artifact io.micrometer micrometer-registry-prometheus 1.13.7 ""
download_artifact io.micrometer micrometer-observation 1.13.7 ""

# Redisson
download_artifact org.redisson redisson-spring-boot-starter 3.24.3 ""
download_artifact org.redisson redisson 3.24.3 ""
download_artifact org.redisson redisson-spring-data-33 3.24.3 ""

# Netty (for Redisson)
download_artifact io.netty netty-common 4.1.115.Final ""
download_artifact io.netty netty-buffer 4.1.115.Final ""
download_artifact io.netty netty-transport 4.1.115.Final ""
download_artifact io.netty netty-handler 4.1.115.Final ""
download_artifact io.netty netty-codec 4.1.115.Final ""

# Additional Gradle plugin dependencies
download_artifact io.spring.gradle dependency-management-plugin 1.1.7 ""
download_artifact com.github.ryarnyah.querydsl com.github.ryarnyah.querydsl.gradle.plugin 0.0.3 ""

echo "Initial dependencies downloaded. Running build to identify missing dependencies..."
