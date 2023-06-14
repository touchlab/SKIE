pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    includeBuild("../build-setup")
    includeBuild("../build-setup-settings")
}

plugins {
    id("dev.settings")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

@Suppress("UNUSED_VARIABLE", "LocalVariableName")
buildSetup {
    val analytics by module
    val configuration by group {
        val annotations by module
        val gradle by module
        val impl by module
    }
    val license by module
    val util by module
}
