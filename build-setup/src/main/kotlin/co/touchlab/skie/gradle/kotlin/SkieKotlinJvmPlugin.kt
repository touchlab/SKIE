package co.touchlab.skie.gradle.kotlin

import co.touchlab.skie.gradle.util.bundles
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.testImplementation
import co.touchlab.skie.gradle.util.versions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class SkieKotlinJvmPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            applyKotlinJvmPlugin()
            setJvmToolchain()
            configureTests()
            configureContextReceivers()
        }
    }

    private fun Project.applyKotlinJvmPlugin() {
        plugins.apply("org.jetbrains.kotlin.jvm")
    }

    private fun Project.setJvmToolchain() {
        extensions.configure(KotlinJvmProjectExtension::class.java) {
            jvmToolchain(libs.versions.java)
        }
    }

    private fun Project.configureTests() {
        tasks.withType(Test::class.java) {
            useJUnitPlatform()
        }

        dependencies {
            testImplementation(libs.bundles.testing.jvm)
        }
    }

    private fun Project.configureContextReceivers() {
        val extension = extensions.create("skieJvm", JvmExtension::class.java)

        afterEvaluate {
            if (extension.areContextReceiversEnabled.get()) {
                tasks.withType(KotlinCompile::class.java).configureEach {
                    kotlinOptions {
                        freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
                    }
                }
            }
        }
    }
}
