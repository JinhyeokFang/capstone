pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "Capstone"

include("domain")
include("common:util")
include("common:infra")
include("common:test")
include("api:capstone-api")
