package co.touchlab.skie.gradle.kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

class SkieKotlinJvmPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            applyKotlinJvmPlugin()
            configureTests()
            configureContextReceivers()
        }
    }

    private fun Project.applyKotlinJvmPlugin() {
        plugins.apply("org.jetbrains.kotlin.jvm")
    }

    private fun Project.configureTests() {
        tasks.withType(Test::class.java) {
            useJUnitPlatform()
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
