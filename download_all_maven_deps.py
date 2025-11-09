#!/usr/bin/env python3
import os
import sys
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

MAVEN_REPO = Path.home() / ".m2" / "repository"
MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2"
downloaded = set()
failed = set()

def download_file(url, dest_path):
    """Download a file using curl"""
    dest_path.parent.mkdir(parents=True, exist_ok=True)
    result = subprocess.run(
        ["curl", "-sf", url, "-o", str(dest_path)],
        capture_output=True
    )
    return result.returncode == 0

def parse_pom(pom_path):
    """Parse POM file and extract dependencies and parent"""
    try:
        tree = ET.parse(pom_path)
        root = tree.getroot()
        ns = {'maven': 'http://maven.apache.org/POM/4.0.0'}

        # Handle namespace
        if root.tag.startswith('{'):
            ns_uri = root.tag[1:root.tag.index('}')]
            ns = {'maven': ns_uri}
        else:
            ns = {}

        deps = []

        # Get parent
        parent = root.find('maven:parent' if ns else 'parent', ns)
        if parent is not None:
            group_id = parent.find('maven:groupId' if ns else 'groupId', ns)
            artifact_id = parent.find('maven:artifactId' if ns else 'artifactId', ns)
            version = parent.find('maven:version' if ns else 'version', ns)
            if group_id is not None and artifact_id is not None and version is not None:
                deps.append((group_id.text, artifact_id.text, version.text, True))

        # Get dependencies
        dependencies = root.find('maven:dependencies' if ns else 'dependencies', ns)
        if dependencies is not None:
            for dep in dependencies.findall('maven:dependency' if ns else 'dependency', ns):
                group_id = dep.find('maven:groupId' if ns else 'groupId', ns)
                artifact_id = dep.find('maven:artifactId' if ns else 'artifactId', ns)
                version = dep.find('maven:version' if ns else 'version', ns)
                scope = dep.find('maven:scope' if ns else 'scope', ns)
                optional = dep.find('maven:optional' if ns else 'optional', ns)

                # Skip test/provided/optional dependencies
                if scope is not None and scope.text in ['test', 'provided']:
                    continue
                if optional is not None and optional.text == 'true':
                    continue

                if group_id is not None and artifact_id is not None and version is not None:
                    # Skip version properties
                    if not version.text.startswith('${'):
                        deps.append((group_id.text, artifact_id.text, version.text, False))

        return deps
    except Exception as e:
        print(f"  Error parsing POM {pom_path}: {e}")
        return []

def download_artifact(group_id, artifact_id, version, is_parent=False, depth=0):
    """Download artifact and its dependencies recursively"""
    key = f"{group_id}:{artifact_id}:{version}"

    if key in downloaded or key in failed:
        return

    if depth > 20:  # Prevent infinite recursion
        return

    indent = "  " * depth
    print(f"{indent}Processing: {key}")

    downloaded.add(key)

    # Calculate paths
    group_path = group_id.replace('.', '/')
    base_path = MAVEN_REPO / group_path / artifact_id / version
    base_url = f"{MAVEN_CENTRAL}/{group_path}/{artifact_id}/{version}"

    # Download POM
    pom_name = f"{artifact_id}-{version}.pom"
    pom_path = base_path / pom_name
    pom_url = f"{base_url}/{pom_name}"

    if not pom_path.exists():
        print(f"{indent}  Downloading POM: {pom_url}")
        if not download_file(pom_url, pom_path):
            print(f"{indent}  Failed to download POM")
            failed.add(key)
            return

    # Download JAR (unless it's a parent POM)
    if not is_parent:
        jar_name = f"{artifact_id}-{version}.jar"
        jar_path = base_path / jar_name
        jar_url = f"{base_url}/{jar_name}"

        if not jar_path.exists():
            print(f"{indent}  Downloading JAR: {jar_url}")
            if not download_file(jar_url, jar_path):
                print(f"{indent}  JAR not found (may be POM-only)")

    # Parse POM and download dependencies
    if pom_path.exists():
        deps = parse_pom(pom_path)
        for dep_group, dep_artifact, dep_version, dep_is_parent in deps:
            download_artifact(dep_group, dep_artifact, dep_version, dep_is_parent, depth + 1)

# Core gradle plugin dependencies
print("Downloading Spring Boot gradle plugin dependencies...")
download_artifact("org.springframework.boot", "spring-boot-gradle-plugin", "3.3.8")

print("\nDownloading Kotlin gradle plugin dependencies...")
download_artifact("org.jetbrains.kotlin", "kotlin-gradle-plugin", "1.9.25")
download_artifact("org.jetbrains.kotlin", "kotlin-allopen", "1.9.25")
download_artifact("org.jetbrains.kotlin", "kotlin-noarg", "1.9.25")

print("\nDownloading complete!")
print(f"Total artifacts downloaded: {len(downloaded)}")
print(f"Total failures: {len(failed)}")
