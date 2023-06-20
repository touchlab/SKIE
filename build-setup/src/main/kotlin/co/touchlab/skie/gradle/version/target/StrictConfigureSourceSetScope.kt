package co.touchlab.skie.gradle.version.target

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class StrictConfigureSourceSetScope(
    override val project: Project,
    override val kotlinSourceSet: KotlinSourceSet,
    private val compilation: MultiDimensionTargetPlugin.Compilation,
): BaseConfigureSourceSetScope() {
    override fun addPlatform(notation: Any) {
        kotlinSourceSet.dependencies {
            val dependency = enforcedPlatform(notation)
            when (compilation) {
                is MultiDimensionTargetPlugin.Compilation.Main -> compileOnly(dependency)
                is MultiDimensionTargetPlugin.Compilation.Test -> implementation(dependency)
            }
        }
    }

    override fun addWeakDependency(dependency: String, configure: ExternalModuleDependency.() -> Unit) {
        kotlinSourceSet.dependencies {
            when (compilation) {
                is MultiDimensionTargetPlugin.Compilation.Main -> compileOnly(dependency, configure)
                is MultiDimensionTargetPlugin.Compilation.Test -> implementation(dependency, configure)
            }
        }
    }

    override fun configureVersion(version: String): ExternalModuleDependency.() -> Unit = {
        version {
            strictly(version)
        }
    }
}
