#!/bin/bash
set -e

MAVEN_REPO="$HOME/.m2/repository"
MAVEN_CENTRAL="https://repo.maven.apache.org/maven2"

download_pom() {
    local group_id=$1
    local artifact_id=$2
    local version=$3

    local group_path=$(echo "$group_id" | tr '.' '/')
    local base_path="$MAVEN_REPO/$group_path/$artifact_id/$version"
    local pom_file="$base_path/${artifact_id}-${version}.pom"

    if [ ! -f "$pom_file" ]; then
        echo "Downloading: $group_id:$artifact_id:$version"
        mkdir -p "$base_path"
        curl -sf "https://repo.maven.apache.org/maven2/$group_path/$artifact_id/$version/${artifact_id}-${version}.pom" -o "$pom_file" 2>/dev/null || echo "  Failed"

        # Also try to download JAR
        local jar_file="$base_path/${artifact_id}-${version}.jar"
        if [ ! -f "$jar_file" ]; then
            curl -sf "https://repo.maven.apache.org/maven2/$group_path/$artifact_id/$version/${artifact_id}-${version}.jar" -o "$jar_file" 2>/dev/null || true
        fi
    fi
}

# Download more parent POMs
download_pom org.apache.httpcomponents.core5 httpcore5-parent 5.2.4
download_pom org.slf4j slf4j-api 1.7.36
download_pom org.sonatype.oss oss-parent 9

# Try to extract more missing deps from gradle output
echo "Running gradle to find more missing dependencies..."
gradle clean build --offline 2>&1 | grep "No cached version of" | grep -oP '(?<=of\s)[^\s]+' | sort -u | while read dep; do
    if [[ $dep == *":"* ]]; then
        group=$(echo "$dep" | cut -d: -f1)
        artifact=$(echo "$dep" | cut -d: -f2)
        version=$(echo "$dep" | cut -d: -f3 | tr -d '.')

        if [ -n "$version" ]; then
            download_pom "$group" "$artifact" "$version"
        fi
    fi
done

echo "Downloaded missing dependencies. Try building again."
