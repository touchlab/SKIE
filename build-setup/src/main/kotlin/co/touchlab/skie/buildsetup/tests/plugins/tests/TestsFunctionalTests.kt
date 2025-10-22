@file:OptIn(ExternalKotlinTargetApi::class, ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.tests.plugins.tests

import co.touchlab.skie.buildsetup.tests.plugins.base.BaseTests
import co.touchlab.skie.buildsetup.tests.tasks.ReformatPackagesInFunctionalTestsTask
import co.touchlab.skie.buildsetup.util.enquoted
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi

abstract class TestsFunctionalTests : Plugin<Project> {

    private val testInputProperties = listOf(
        "failedOnly",
        "acceptanceTest",
    )

    override fun apply(project: Project) = with(project) {
        apply<BaseTests>()

        configureBuildConfig()
        configureTestTask()
        configureReformatPackagesTask()
    }

    private fun Project.configureBuildConfig() {
        val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer
        val testSourceSet = sourceSets.named("test")

        extensions.configure<BuildConfigExtension> {
            buildConfigField(
                type = "String",
                name = "TEST_RESOURCES",
                value = testSourceSet.map { it.output.resourcesDir!!.absolutePath.enquoted() },
            )
        }
    }

    private fun Project.configureTestTask() {
        tasks.named("test", Test::class.java).configure {
            testInputProperties.forEach {
                inputs.property(it, System.getenv(it)).optional(true)
            }
        }
    }

    private fun Project.configureReformatPackagesTask() {
        tasks.register<ReformatPackagesInFunctionalTestsTask>("reformatPackagesInFunctionalTests") {
            resourcesDirectory = layout.projectDirectory.dir("src/test/resources").asFile
        }
    }
}
