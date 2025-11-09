#!/usr/bin/env python3
import os
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path
import time

MAVEN_REPO = Path.home() / ".m2" / "repository"
MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2"
downloaded = set()
failed = set()
MAX_DEPTH = 30

def download_file(url, dest_path):
    """Download a file using curl"""
    dest_path.parent.mkdir(parents=True, exist_ok=True)
    result = subprocess.run(
        ["curl", "-sf", url, "-o", str(dest_path)],
        capture_output=True,
        timeout=30
    )
    return result.returncode == 0

def parse_pom_dependencies(pom_path):
    """Parse POM and extract ALL dependencies including dependency management"""
    try:
        tree = ET.parse(pom_path)
        root = tree.getroot()

        # Determine namespace
        ns_match = root.tag[root.tag.find('{'):root.tag.find('}')+1] if root.tag.startswith('{') else ''
        ns = {'m': ns_match[1:-1]} if ns_match else {}

        def get_text(element, tag):
            if ns:
                elem = element.find(f'm:{tag}', ns)
            else:
                elem = element.find(tag)
            return elem.text.strip() if elem is not None and elem.text else None

        deps = []

        # Get parent
        parent = root.find('m:parent' if ns else 'parent', ns)
        if parent is not None:
            g = get_text(parent, 'groupId')
            a = get_text(parent, 'artifactId')
            v = get_text(parent, 'version')
            if g and a and v and not v.startswith('${'):
                deps.append((g, a, v, True))

        # Get dependencies from <dependencies>
        dependencies = root.find('m:dependencies' if ns else 'dependencies', ns)
        if dependencies is not None:
            for dep in dependencies.findall('m:dependency' if ns else 'dependency', ns):
                g = get_text(dep, 'groupId')
                a = get_text(dep, 'artifactId')
                v = get_text(dep, 'version')

                if g and a and v and not v.startswith('${'):
                    deps.append((g, a, v, False))

        # Get dependencies from <dependencyManagement>
        dep_mgmt = root.find('m:dependencyManagement' if ns else 'dependencyManagement', ns)
        if dep_mgmt is not None:
            dep_mgmt_deps = dep_mgmt.find('m:dependencies' if ns else 'dependencies', ns)
            if dep_mgmt_deps is not None:
                for dep in dep_mgmt_deps.findall('m:dependency' if ns else 'dependency', ns):
                    g = get_text(dep, 'groupId')
                    a = get_text(dep, 'artifactId')
                    v = get_text(dep, 'version')

                    # Also download dependency management POMs (many are BOMs)
                    if g and a and v and not v.startswith('${'):
                        deps.append((g, a, v, True))  # Mark as parent to skip JAR

        return deps
    except Exception as e:
        return []

def download_artifact(group_id, artifact_id, version, is_parent=False, depth=0):
    """Download artifact recursively"""
    key = f"{group_id}:{artifact_id}:{version}"

    if key in downloaded or key in failed or depth > MAX_DEPTH:
        return

    downloaded.add(key)

    group_path = group_id.replace('.', '/')
    base_path = MAVEN_REPO / group_path / artifact_id / version
    base_url = f"{MAVEN_CENTRAL}/{group_path}/{artifact_id}/{version}"

    # Download POM
    pom_name = f"{artifact_id}-{version}.pom"
    pom_path = base_path / pom_name
    pom_url = f"{base_url}/{pom_name}"

    if not pom_path.exists():
        if depth < 3:
            print(f"{'  ' * depth}[{depth}] {key}")
        if not download_file(pom_url, pom_path):
            failed.add(key)
            return

    # Download JAR if not a parent
    if not is_parent:
        jar_name = f"{artifact_id}-{version}.jar"
        jar_path = base_path / jar_name
        jar_url = f"{base_url}/{jar_name}"

        if not jar_path.exists():
            download_file(jar_url, jar_path)

    # Parse and recursively download dependencies
    if pom_path.exists():
        deps = parse_pom_dependencies(pom_path)
        for dep_g, dep_a, dep_v, dep_is_parent in deps:
            download_artifact(dep_g, dep_a, dep_v, dep_is_parent, depth + 1)

# Start with buildscript dependencies
print("=" * 60)
print("Downloading all Gradle buildscript dependencies recursively...")
print("=" * 60)

start_deps = [
    ("org.springframework.boot", "spring-boot-gradle-plugin", "3.3.8"),
    ("org.jetbrains.kotlin", "kotlin-gradle-plugin", "1.9.25"),
    ("org.jetbrains.kotlin", "kotlin-allopen", "1.9.25"),
    ("org.jetbrains.kotlin", "kotlin-noarg", "1.9.25"),
]

for g, a, v in start_deps:
    download_artifact(g, a, v, False)

print("\n" + "=" * 60)
print(f"Download complete!")
print(f"Total downloaded: {len(downloaded)}")
print(f"Total failed: {len(failed)}")
print("=" * 60)
