#!/usr/bin/env python3
import os
import sys
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

MAVEN_REPO = Path.home() / ".m2" / "repository"
MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2"
downloaded = set()

def download_file(url, dest_path):
    """Download a file using curl"""
    dest_path.parent.mkdir(parents=True, exist_ok=True)
    result = subprocess.run(
        ["curl", "-sf", url, "-o", str(dest_path)],
        capture_output=True
    )
    return result.returncode == 0

def download_artifact(group_id, artifact_id, version):
    """Download artifact POM (no JAR)"""
    key = f"{group_id}:{artifact_id}:{version}"

    if key in downloaded:
        return

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
        print(f"Downloading: {key}")
        download_file(pom_url, pom_path)

# Download missing parent POMs and test dependencies
artifacts = [
    ("org.junit", "junit-bom", "5.10.0"),
    ("org.junit", "junit-bom", "5.10.1"),
    ("org.antlr", "antlr4-master", "4.7.2"),
    ("org.sonatype.oss", "oss-parent", "7"),
]

print("Downloading parent POMs...")
for group_id, artifact_id, version in artifacts:
    download_artifact(group_id, artifact_id, version)

print("Done!")
