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



// includeBuild("plugin")

// includeBuild("common")
// includeBuild("skie-gradle")
// includeBuild("website")

// include(
//     ":acceptance-tests:framework",
//     ":acceptance-tests:external-libraries",
//     ":acceptance-tests:type-mapping",
//     ":acceptance-tests:type-mapping:exported-dependency",
//     ":acceptance-tests:type-mapping:nonexported-dependency",
//     ":acceptance-tests",
// )
//
@Suppress("UNUSED_VARIABLE", "LocalVariableName")
buildSetup {
    val common by module {
        val analytics by module
        val configuration by group {
            val annotations by module
            val gradle by module
            val impl by module
        }
        val license by module
        val util by module
    }

    val server by module {
        val shared by group {
            val api by module
        }
        val license by group {
            val api by module
            val ui by module
            val impl by module
        }
        val analytics by group {
            val ui by module
        }
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

}
