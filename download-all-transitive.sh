#!/bin/bash
set -e

MAVEN_REPO="$HOME/.m2/repository"
mkdir -p "$MAVEN_REPO"
Downloaded=()

download_artifact() {
    local group_id=$1
    local artifact_id=$2
    local version=$3
    local classifier=$4
    local skip_jar=$5

    local key="$group_id:$artifact_id:$version"

    # Skip if already downloaded
    for item in "${Downloaded[@]}"; do
        if [ "$item" == "$key" ]; then
            return 0
        fi
    done
    Downloaded+=("$key")

    local group_path=$(echo "$group_id" | tr '.' '/')
    local base_path="$MAVEN_REPO/$group_path/$artifact_id/$version"
    local base_url="https://repo.maven.apache.org/maven2/$group_path/$artifact_id/$version"

    mkdir -p "$base_path"

    # Download POM
    local pom_file="$base_path/${artifact_id}-${version}.pom"
    if [ ! -f "$pom_file" ]; then
        echo "Downloading POM: $group_id:$artifact_id:$version"
        if curl -sf "$base_url/${artifact_id}-${version}.pom" -o "$pom_file" 2>/dev/null; then
            # Parse POM for parent and dependencies
            if [ -f "$pom_file" ]; then
                # Extract parent
                local parent_group=$(grep -oP '<parent>.*?<groupId>\K[^<]+' "$pom_file" 2>/dev/null | head -1 || true)
                local parent_artifact=$(grep -oP '<parent>.*?<artifactId>\K[^<]+' "$pom_file" 2>/dev/null | head -1 || true)
                local parent_version=$(grep -oP '<parent>.*?<version>\K[^<]+' "$pom_file" 2>/dev/null | head -1 || true)

                if [ -n "$parent_group" ] && [ -n "$parent_artifact" ] && [ -n "$parent_version" ]; then
                    download_artifact "$parent_group" "$parent_artifact" "$parent_version" "" "skip_jar"
                fi
            fi
        fi
    fi

    # Download JAR unless skip_jar is set
    if [ "$skip_jar" != "skip_jar" ]; then
        local jar_file="$base_path/${artifact_id}-${version}${classifier:+-}${classifier}.jar"
        if [ ! -f "$jar_file" ]; then
            echo "Downloading JAR: $group_id:$artifact_id:$version${classifier:+:}${classifier}"
            curl -sf "$base_url/${artifact_id}-${version}${classifier:+-}${classifier}.jar" -o "$jar_file" 2>/dev/null || true
        fi
    fi
}

# Download more parent POMs
download_artifact org.apache apache 30 "" skip_jar
download_artifact com.fasterxml.jackson jackson-base 2.14.2 "" skip_jar
download_artifact com.fasterxml.jackson.module jackson-modules-java8 2.14.2 "" skip_jar
download_artifact org.apache.httpcomponents.client5 httpclient5-parent 5.3.1 "" skip_jar

# More Kotlin
download_artifact org.jetbrains.kotlin kotlin-util-io 1.9.25 ""
download_artifact org.jetbrains.kotlin kotlin-daemon-client 1.9.25 ""
download_artifact org.jetbrains.kotlinx kotlinx-coroutines-core-jvm 1.5.0 ""

# ANTLR and other deps
download_artifact org.antlr antlr4-runtime 4.7.2 ""
download_artifact com.google.code.findbugs jsr305 3.0.2 ""

echo "Transitive dependencies downloaded!"
