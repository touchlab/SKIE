@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.main.plugins.utility

import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import co.touchlab.skie.buildsetup.util.version.KotlinVersionSet
import co.touchlab.skie.gradle.KotlinCompilerVersionAttribute
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

abstract class UtilityMultiKotlinVersionSupportPlugin : Plugin<Project> {

    // WIP
// Source sets
// kotlinExtension.sourceSets.maybeCreate(sourceSet.name + "__" + compilation.sourceSetNameSuffix)
// } else {
//     kotlinExtension.sourceSets.getByName(sourceSet.name + compilation.sourceSetNameSuffix)

    override fun apply(target: Project): Unit = with(target) {
        apply<KotlinPluginWrapper>()

        val versionSourceSets = getVersionSourceSets()

        configureActiveSourceSets(versionSourceSets)

        configureKotlinCompilerVersionAttributeForOutgoingVariants(project)
    }

    private fun Project.getVersionSourceSets(): List<KotlinVersionSet> {
        val kotlinVersionSets = layout.projectDirectory.dir("src").asFile.toPath()
            .listDirectoryEntries()
            .filter { it.isDirectory() }
            .filter { it.name.startsWith("..") || it.name.first().isDigit() }
            .map { KotlinVersionSet.from(it) }

        val supportedVersionNames = SupportedKotlinVersionProvider.getSupportedKotlinVersions(project).map { it.name }
        val invalidSourceSets = kotlinVersionSets.filterNot { it.isValid(supportedVersionNames) }
        check(invalidSourceSets.isEmpty()) {
            "The following version source sets are invalid because they reference unsupported versions: " +
                invalidSourceSets.joinToString { it.path.name }
        }

        return kotlinVersionSets
    }

    private fun Project.configureActiveSourceSets(kotlinVersionSets: List<KotlinVersionSet>) {
        val activeVersionName = SupportedKotlinVersionProvider.getPrimaryKotlinVersion(project).name

        val activeSourceSets = kotlinVersionSets.filter { it.isActive(activeVersionName) }

        extensions.configure<KotlinJvmProjectExtension> {
            val mainSourceSet = sourceSets.getByName("main")

            activeSourceSets.forEach {
                mainSourceSet.kotlin.srcDir(it.path.resolve("kotlin"))
                mainSourceSet.resources.srcDir(it.path.resolve("resources"))
            }
        }
    }

    private fun configureKotlinCompilerVersionAttributeForOutgoingVariants(project: Project) {
        val activeKotlinVersionName = SupportedKotlinVersionProvider.getPrimaryKotlinVersion(project).name

        project.configurations.configureEach {
            if (name.endsWith("Elements")) {
                attributes {
                    attribute(KotlinCompilerVersionAttribute.attribute, project.objects.named(activeKotlinVersionName.toString()))
                }
            }
        }
    }
}

