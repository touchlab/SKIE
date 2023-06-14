rootProject.name = "skie-gradle"

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
    val `gradle-plugin` by module("plugin")
    val `kotlin-gradle-plugin-shim` by module {
        val `kotlin-gradle-plugin-shim-impl` by module("impl")
    }
}
