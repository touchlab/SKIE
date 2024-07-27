package co.touchlab.skie.gradle.version.target

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class DefaultConfigureSourceSetScope(
    private val project: Project,
    override val kotlinSourceSet: KotlinSourceSet,
    override val compilation: MultiDimensionTargetPlugin.Compilation,
) : ConfigureSourceSetScope {

    override fun dependencies(block: MultiDimensionTargetDependencyHandler.() -> Unit) {
        kotlinSourceSet.dependencies {
            with(DependencyHandler(this)) {
                block()
            }
        }
    }

    override fun configureRelatedConfigurations(block: Configuration.() -> Unit) {
        kotlinSourceSet.relatedConfigurationNames.forEach {
            project.configurations.named(it).configure(block)
        }
    }

    private val KotlinSourceSet.relatedConfigurationNames: List<String>
        get() = listOf(apiConfigurationName, implementationConfigurationName, compileOnlyConfigurationName, runtimeOnlyConfigurationName)

    private inner class DependencyHandler(
        kotlinDependencyHandler: KotlinDependencyHandler,
    ) : MultiDimensionTargetDependencyHandler, KotlinDependencyHandler by kotlinDependencyHandler {

        override fun weak(dependencyNotation: Any): Dependency? = compilation(
            main = { compileOnly(dependencyNotation) },
            test = { implementation(dependencyNotation) },
        )

        override fun weak(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency = compilation(
            main = { compileOnly(dependencyNotation, configure) },
            test = { implementation(dependencyNotation, configure) },
        )

        override fun <T : Dependency> weak(dependency: T, configure: T.() -> Unit): T = compilation(
            main = { compileOnly(dependency, configure) },
            test = { implementation(dependency, configure) },
        )

        override fun testOnly(dependencyNotation: Any): Dependency? = compilation(
            main = { null },
            test = { implementation(dependencyNotation) },
        )

        override fun testOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency? = compilation(
            main = { null },
            test = { implementation(dependencyNotation, configure) },
        )

        override fun <T : Dependency> testOnly(dependency: T, configure: T.() -> Unit): T? = compilation(
            main = { null },
            test = { implementation(dependency, configure) },
        )

        private fun <T> compilation(
            main: () -> T,
            test: () -> T,
        ): T = when (compilation) {
            is MultiDimensionTargetPlugin.Compilation.Main -> main()
            is MultiDimensionTargetPlugin.Compilation.Test -> test()
        }
    }
}
