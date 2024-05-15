package co.touchlab.skie.plugin

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.shim.KonanTargetShim
import co.touchlab.skie.plugin.shim.KotlinSourceSetShim
import co.touchlab.skie.plugin.util.KotlinCompilerPluginOption
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

sealed interface SkieTarget {

    val name: String

    val project: Project

    val task: TaskProvider<out Task>

    val konanTarget: KonanTargetShim

    val outputKind: OutputKind

    val requiredConfigurationFlags: Set<SkieConfigurationFlag>

    val skieDirectories: Provider<SkieDirectories>

    val freeCompilerArgs: Provider<List<String>>

    fun addPluginArgument(pluginId: String, option: KotlinCompilerPluginOption)

    fun addToCompilerClasspath(fileCollection: FileCollection)

    fun addFreeCompilerArgs(vararg args: String)

    interface Binary : SkieTarget {

        val compileDependencyConfigurationName: String

        val allCompilationKotlinSourceSets: List<KotlinSourceSetShim>
    }

    interface Artifact : SkieTarget {

        val fullArtifactName: String
    }

    enum class OutputKind {
        Framework,
        XCFramework,
        UniversalFramework,
        Library,
    }
}

val SkieTarget.skieBuildDirectory: Provider<SkieBuildDirectory>
    get() = skieDirectories.map { it.buildDirectory }
