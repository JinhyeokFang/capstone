#!/bin/bash
set -e

MAVEN_REPO="$HOME/.m2/repository"
mkdir -p "$MAVEN_REPO"

download_artifact() {
    local group_id=$1
    local artifact_id=$2
    local version=$3
    local classifier=$4
    local skip_jar=$5

    local group_path=$(echo "$group_id" | tr '.' '/')
    local base_path="$MAVEN_REPO/$group_path/$artifact_id/$version"
    local base_url="https://repo.maven.apache.org/maven2/$group_path/$artifact_id/$version"

    mkdir -p "$base_path"

    # Download POM
    local pom_file="$base_path/${artifact_id}-${version}.pom"
    if [ ! -f "$pom_file" ]; then
        echo "Downloading POM: $group_id:$artifact_id:$version"
        curl -sf "$base_url/${artifact_id}-${version}.pom" -o "$pom_file" 2>/dev/null || echo "  Failed to download POM"
    fi

    # Download JAR unless skip_jar is set
    if [ "$skip_jar" != "skip_jar" ]; then
        local jar_file="$base_path/${artifact_id}-${version}${classifier:+-}${classifier}.jar"
        if [ ! -f "$jar_file" ]; then
            echo "Downloading JAR: $group_id:$artifact_id:$version${classifier:+:}${classifier}"
            curl -sf "$base_url/${artifact_id}-${version}${classifier:+-}${classifier}.jar" -o "$jar_file" 2>/dev/null || echo "  Failed to download JAR"
        fi
    fi
}

# Parent POMs (no JAR)
download_artifact org.apache.commons commons-parent 64 "" skip_jar
download_artifact org.apache apache 31 "" skip_jar

# Apache Commons correct version
download_artifact org.apache.commons commons-compress 1.25.0 ""

# Kotlin gradle plugin dependencies
download_artifact org.jetbrains.kotlin kotlin-gradle-plugin-idea-proto 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-util-klib 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-klib-commonizer-api 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-build-tools-api 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-android-extensions 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-compiler-runner 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-scripting-compiler-embeddable 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-scripting-compiler-impl-embeddable 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-gradle-plugin-annotations 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-native-utils 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-project-model 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-tooling-core 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-daemon-embeddable 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-gradle-plugin-idea 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-scripting-common 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-scripting-jvm 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-sam-with-receiver 1.9.25 ""

# Jackson 2.14.2 (for buildpack platform)
download_artifact com.fasterxml.jackson.core jackson-databind 2.14.2 ""
download_artifact com.fasterxml.jackson.core jackson-core 2.14.2 ""
download_artifact com.fasterxml.jackson.core jackson-annotations 2.14.2 ""
download_artifact com.fasterxml.jackson.module jackson-module-parameter-names 2.14.2 ""

# JNA
download_artifact net.java.dev.jna jna-platform 5.13.0 ""
download_artifact net.java.dev.jna jna 5.13.0 ""

# HttpClient 5
download_artifact org.apache.httpcomponents.client5 httpclient5 5.3.1 ""
download_artifact org.apache.httpcomponents.core5 httpcore5 5.2.4 ""
download_artifact org.apache.httpcomponents.core5 httpcore5-h2 5.2.4 ""

# TOML
download_artifact org.tomlj tomlj 1.0.0 ""

# IntelliJ dependencies
download_artifact org.jetbrains.intellij.deps trove4j 1.0.20200330 ""

# More Spring dependencies
download_artifact org.springframework spring-framework-bom 6.1.14 "" skip_jar
download_artifact org.springframework.boot spring-boot-starter-logging 3.3.8 ""

# SLF4J
download_artifact org.slf4j slf4j-api 2.0.13 ""

echo "Missing dependencies downloaded!"
