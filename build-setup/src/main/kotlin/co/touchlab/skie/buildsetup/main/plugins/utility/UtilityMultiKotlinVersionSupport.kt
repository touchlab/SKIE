@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.main.plugins.utility

import co.touchlab.skie.buildsetup.util.version.VersionSourceSet
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersionProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

abstract class UtilityMultiKotlinVersionSupport : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<KotlinPluginWrapper>()

        val versionSourceSets = getVersionSourceSets()

        configureActiveSourceSets(versionSourceSets)
    }

    private fun Project.getVersionSourceSets(): List<VersionSourceSet> {
        val versionSourceSets = layout.projectDirectory.dir("src").asFile.toPath()
            .listDirectoryEntries()
            .filter { it.isDirectory() }
            .filter { it.name.startsWith("..") || it.name.first().isDigit() }
            .map { VersionSourceSet.from(it) }

        val supportedVersionNames = KotlinToolingVersionProvider.getSupportedKotlinToolingVersions(project).map { it.name }
        val invalidSourceSets = versionSourceSets.filterNot { it.isValid(supportedVersionNames) }
        check(invalidSourceSets.isEmpty()) {
            "The following version source sets are invalid because they reference unsupported versions: " +
                invalidSourceSets.joinToString { it.path.name }
        }

        return versionSourceSets
    }

    private fun Project.configureActiveSourceSets(versionSourceSets: List<VersionSourceSet>) {
        val activeVersionName = KotlinToolingVersionProvider.getActiveKotlinToolingVersion(project).name

        val activeSourceSets = versionSourceSets.filter { it.isActive(activeVersionName) }

        extensions.configure<KotlinJvmProjectExtension> {
            val mainSourceSet = sourceSets.getByName("main")

            activeSourceSets.forEach {
                mainSourceSet.kotlin.srcDir(it.path.resolve("kotlin"))
                mainSourceSet.resources.srcDir(it.path.resolve("resources"))
            }
        }
    }
}

