package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

class SkieServer: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<KotlinPluginWrapper>()

        group = "co.touchlab.skie.server"

        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(libs.versions.java)
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }

        dependencies {
            testImplementation(libs.bundles.testing.jvm)
        }
    }
}
