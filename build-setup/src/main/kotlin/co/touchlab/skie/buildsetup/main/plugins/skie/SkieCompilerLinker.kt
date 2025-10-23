@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")

package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersion
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMultiKotlinVersionSupport
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityOptInExperimentalCompilerApi
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersionProvider
import co.touchlab.skie.gradle.util.compileOnly
import co.touchlab.skie.gradle.util.withKotlinNativeCompilerEmbeddableDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class SkieCompilerLinker : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlin>()
        apply<UtilityMultiKotlinVersionSupport>()
        apply<UtilityMinimumTargetKotlinVersion>()
        apply<UtilityOptInExperimentalCompilerApi>()
        apply<KotlinPluginWrapper>()

        addDependencyOnCompiler()

        project.dependencies {
            compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
        }
    }

    private fun Project.addDependencyOnCompiler() {
        val primaryVersion = KotlinToolingVersionProvider.getActiveKotlinToolingVersion(project).primaryVersion

        withKotlinNativeCompilerEmbeddableDependency(primaryVersion, isTarget = true) { dependency ->
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
