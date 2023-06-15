package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.util.implementation
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

class SkieCommon: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<KotlinPluginWrapper>()

        group = "co.touchlab.skie"

        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(libs.versions.java)
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }

        dependencies {
            implementation(kotlin("stdlib"))
            testImplementation(libs.bundles.testing.jvm)
        }
    }
}
