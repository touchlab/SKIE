@file:Suppress("UnstableApiUsage")

package co.touchlab.skie.buildsetup.settings.plugins

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.maven
import java.net.URI

class SettingsGradlePlugin : Plugin<Settings> {

    override fun apply(target: Settings) = with(target) {
        dependencyResolutionManagement {
            repositories {
                maven("https://api.touchlab.dev/public") {
                    content {
                        includeModule("org.jetbrains.kotlin", "kotlin-native-compiler-embeddable")
                    }
                }
                mavenCentral()
                google {
                    content {
                        includeGroupByRegex("com\\.google(?:\\..+|\\Z)")
                        includeGroupByRegex("androidx\\..*")
                    }
                }
                maven { url = URI("https://repo.gradle.org/gradle/libs-releases") }
                gradlePluginPortal()
            }
        }

        enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
    }
}
