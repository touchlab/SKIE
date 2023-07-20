rootProject.name = "SKIE"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    includeBuild("../build-setup-settings")
    includeBuild("../build-setup")
}

plugins {
    id("dev.settings")
}

dependencyResolutionManagement {
    repositories {
        maven("https://api.touchlab.dev/public") {
            content {
                includeModule("org.jetbrains.kotlin", "kotlin-native-compiler-embeddable")
            }
        }
        mavenCentral()
        google()
    }
}

@Suppress("UNUSED_VARIABLE", "LocalVariableName")
buildSetup {
    val common by module {
        val analytics by module
        val configuration by group {
            val annotations by module
            val gradle by module
            val impl by module
        }
        val util by module
    }

    val runtime by group {
        val kotlin by module
        val swift by module
    }

    val compiler by module {
        val `kotlin-plugin` by module
    }

    val gradle by group("skie-gradle") {
        val plugin by module
        val `plugin-api` by module
        val `plugin-loader` by module
    }

    val `acceptance-tests` by module {
        val `acceptance-tests-framework` by module("framework")

        val `test-dependencies` by module {
            val `regular-dependency` by module("regular")
            val `exported-dependency` by module("exported")
        }
    }
}
