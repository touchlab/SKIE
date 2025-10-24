@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")

package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.extensions.MultiKotlinVersionSupportExtension
import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMultiKotlinVersionSupportPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityOptInExperimentalCompilerApiPlugin
import co.touchlab.skie.buildsetup.util.compileOnly
import co.touchlab.skie.buildsetup.util.version.KotlinCompilerVersionEnumGenerator
import co.touchlab.skie.buildsetup.util.withKotlinNativeCompilerEmbeddableDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
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
        generateKotlinVersionEnum()

        project.dependencies {
            compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
        }
    }

    private fun Project.addDependencyOnCompiler() {
        extensions.configure<MultiKotlinVersionSupportExtension> {
            compilations.configureEach {
                withKotlinNativeCompilerEmbeddableDependency(supportedKotlinVersion.compilerVersion) { dependency ->
                    val kotlinCompilerApiConfiguration = project.configurations.detachedConfiguration(dependency)

                    dependencies {
                        add(kotlinCompilation.compileOnlyConfigurationName, dependency)
                    }

                    kotlinCompilation.compileTaskProvider.configure {
                        (this as KotlinCompile).friendPaths.from(kotlinCompilerApiConfiguration)
                    }
                }
            }
        }
    }

    private fun Project.generateKotlinVersionEnum() {
        extensions.configure<MultiKotlinVersionSupportExtension> {
            compilations.configureEach {
                KotlinCompilerVersionEnumGenerator.generate(
                    kotlinSourceSet = kotlinCompilation.defaultSourceSet,
                    packageName = "co.touchlab.skie",
                    makeEnumPublic = true,
                    activeVersion = supportedKotlinVersion,
                )
            }
        }
    }
}
