rootProject.name = "build-setup"

pluginManagement {
    includeBuild("../build-setup-gradle-settings")
}

plugins {
    id("settings.gradle")
}

include(":shared")
