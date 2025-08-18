rootProject.name = "build-setup"

pluginManagement {
    includeBuild("../build-setup-gradle-settings")
    includeBuild("kotlin-tooling-version")
}

plugins {
    id("dev.gradle.settings")
}

includeBuild("kotlin-tooling-version")
