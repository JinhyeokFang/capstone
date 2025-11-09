pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "Capstone"

include("domain")
include("common:util")
include("common:infra")
include("common:test")
include("api:capstone-api")
