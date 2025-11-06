@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")

package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.extensions.MultiKotlinVersionSupportExtension
import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMultiKotlinVersionSupportPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityOptInCompilerApiPlugin
import co.touchlab.skie.buildsetup.main.tasks.GenerateKotlinVersionEnumTask
import co.touchlab.skie.buildsetup.util.compileOnly
import co.touchlab.skie.buildsetup.util.getKotlinNativeCompilerEmbeddableDependency
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
        apply<UtilityOptInCompilerApiPlugin>()
        apply<KotlinPluginWrapper>()

        addDependencyOnCompiler()
        generateKotlinVersionEnum()
    }

    private fun Project.addDependencyOnCompiler() {
        extensions.configure<MultiKotlinVersionSupportExtension> {
            compilations.configureEach {
                val compilerDependency = getKotlinNativeCompilerEmbeddableDependency(supportedKotlinVersion.compilerVersion)

                val kotlinCompilerApiConfiguration = project.configurations.detachedConfiguration(compilerDependency)

                dependencies {
                    add(kotlinCompilation.compileOnlyConfigurationName, compilerDependency)
                    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
                }

                kotlinCompilation.compileTaskProvider.configure {
                    (this as KotlinCompile).friendPaths.from(kotlinCompilerApiConfiguration)
                }
            }
        }
    }

    private fun Project.generateKotlinVersionEnum() {
        extensions.configure<MultiKotlinVersionSupportExtension> {
            compilations.configureEach {
                GenerateKotlinVersionEnumTask.register(
                    kotlinCompilation = kotlinCompilation,
                    packageName = "co.touchlab.skie",
                    makeEnumPublic = true,
                    activeVersion = supportedKotlinVersion,
                )
            }
        }
    }
}
