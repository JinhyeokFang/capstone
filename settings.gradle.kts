rootProject.name = "Capstone"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("domain")
include("common:util")
include("common:infra")
include("common:test")
include("api:capstone-api")
