rootProject.name = "dev-support-build-setup"

pluginManagement {
    includeBuild("../../build-setup-gradle-settings")
}

plugins {
    id("settings.gradle")
}

includeBuild("../../build-setup") {
    dependencySubstitution {
        substitute(module("co.touchlab.skie:build-setup-shared")).using(project(":shared"))
    }
}
