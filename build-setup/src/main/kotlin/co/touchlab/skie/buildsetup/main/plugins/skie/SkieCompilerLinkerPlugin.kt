@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")

package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMultiKotlinVersionSupportPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityOptInExperimentalCompilerApiPlugin
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersionProvider
import co.touchlab.skie.buildsetup.util.compileOnly
import co.touchlab.skie.buildsetup.util.withKotlinNativeCompilerEmbeddableDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class SkieCompilerLinkerPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlinPlugin>()
        apply<UtilityMultiKotlinVersionSupportPlugin>()
        apply<UtilityMinimumTargetKotlinVersionPlugin>()
        apply<UtilityOptInExperimentalCompilerApiPlugin>()
        apply<KotlinPluginWrapper>()

        addDependencyOnCompiler()

        project.dependencies {
            compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
        }
    }

    private fun Project.addDependencyOnCompiler() {
        val primaryVersion = KotlinToolingVersionProvider.getActiveKotlinToolingVersion(project).primaryVersion

        withKotlinNativeCompilerEmbeddableDependency(primaryVersion) { dependency ->
            val kotlinCompilerApiConfiguration = project.configurations.detachedConfiguration(dependency)

            dependencies {
                compileOnly(dependency)
            }

            extensions.configure<KotlinJvmProjectExtension> {
                target.compilations.getByName("main").compileTaskProvider.configure {
                    (this as KotlinCompile).friendPaths.from(kotlinCompilerApiConfiguration)
                }
            }
        }
    }
}
