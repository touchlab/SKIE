package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.maven

class DevSettings: Plugin<Settings> {
    override fun apply(target: Settings) = with(target) {
        dependencyResolutionManagement {
            repositories {
                maven("https://api.touchlab.dev/public") {
                    content {
                        includeModule("org.jetbrains.kotlin", "kotlin-native-compiler-embeddable")
                    }
                }
                mavenCentral()
            }
        }

        enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
    }
}
