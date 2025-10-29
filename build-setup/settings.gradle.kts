rootProject.name = "build-setup"

pluginManagement {
    includeBuild("../build-setup-gradle-settings")
}

plugins {
    id("dev.gradle.settings")
}

include(":shared")
